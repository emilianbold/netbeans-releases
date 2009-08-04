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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.spi.viewmodel;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.netbeans.modules.viewmodel.DefaultTreeExpansionManager;
import org.netbeans.modules.viewmodel.OutlineTable;
import org.netbeans.modules.viewmodel.TreeModelRoot;

import org.openide.awt.Actions;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;


/**
 * Contains various utility methods for various models.
 *
 * @author   Jan Jancura
 */
public final class Models {

    /** Cached default implementations of expansion models. */
    private static WeakHashMap<Object, DefaultTreeExpansionModel> defaultExpansionModels = new WeakHashMap<Object, DefaultTreeExpansionModel>();
    /**
     * Empty model - returns default root node with no children.
     */
    public static CompoundModel EMPTY_MODEL = createCompoundModel 
        (new ArrayList ());
    
    
    public static int MULTISELECTION_TYPE_EXACTLY_ONE = 1;
    public static int MULTISELECTION_TYPE_ALL = 2;
    public static int MULTISELECTION_TYPE_ANY = 3;
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.models") != null;
    
    
    /**
     * Creates a new instance of TreeTableView
     * for given {@link org.netbeans.spi.viewmodel.Models.CompoundModel}.
     *
     * @param compoundModel a compound model instance
     *
     * @return new instance of complete model view
     */
    public static JComponent createView (
        CompoundModel compoundModel
    ) {
        OutlineTable ot = new OutlineTable ();
        ot.setModel (compoundModel);
        return ot;
    }
    
    /**
     * Creates a root node of the nodes tree structure
     * for given {@link org.netbeans.spi.viewmodel.Models.CompoundModel}.
     *
     * @param compoundModel a compound model instance
     * @param treeView The tree view component where nodes are going to be displayed.
     *
     * @return new instance root node
     * @since 1.15
     */
    public static Node createNodes (
        CompoundModel compoundModel,
        TreeView treeView
    ) {
        return new TreeModelRoot (compoundModel, treeView).getRootNode();
    }
    
    /**
     * Set given models to given view instance.
     *
     * @param view a view instance - must be an instance created by {@link #createView} method.
     * @param compoundModel a compound model instance
     */
    public static void setModelsToView (
        final JComponent view,
        final CompoundModel compoundModel
    ) {
        if (!(view instanceof OutlineTable)) {
            throw new IllegalArgumentException("Expecting an instance of "+OutlineTable.class.getName()+", which can be obtained from Models.createView(). view = "+view);
        }
        if (verbose)
            System.out.println (compoundModel);
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                ((OutlineTable) view).setModel (compoundModel);
            }
        });
    }
    
    /**
     * Creates one {@link CompoundModel} from given list of models.
     * <p>
     * If this list does not include any instance of TreeModel or
     * TreeExpansionModel, a default implementation of these models is created
     * based on the instance of this list. Thus you get one implementation
     * per provided instance of the models list.
     * 
     * @param models a list of models
     * @return {@link CompoundModel} encapsulating given list of models
     */
    public static CompoundModel createCompoundModel (List models) {
        return createCompoundModel(models, null);
    }
    
    /**
     * Creates one {@link CompoundModel} from given list of models.
     * <p>
     * If this list does not include any instance of TreeModel or
     * TreeExpansionModel, a default implementation of these models is created
     * based on the instance of this list. Thus you get one implementation
     * per provided instance of the models list.
     * 
     * @param models a list of models
     * @param propertiesHelpID The help ID, which is set for the properties
     *        sheets created from this model.
     * @return {@link CompoundModel} encapsulating given list of models
     * @since 1.7
     */
    // TODO: Add createCompoundModel(List models, String propertiesHelpID, RequestProcessor rp)
    // Or instead of RP use some interface that could provide the desired thread to run in (current, AWT, RP thread,...)
    public static CompoundModel createCompoundModel (List models, String propertiesHelpID) {
        List<TreeModel>                 treeModels;
        List<TreeModelFilter>           treeModelFilters;
        List<TreeExpansionModel>        treeExpansionModels;
        List<TreeExpansionModelFilter>  treeExpansionModelFilters;
        List<NodeModel>                 nodeModels;
        List<NodeModelFilter>           nodeModelFilters;
        List<TableModel>                tableModels;
        List<TableModelFilter>          tableModelFilters;
        List<NodeActionsProvider>       nodeActionsProviders;
        List<NodeActionsProviderFilter> nodeActionsProviderFilters;
        List<ColumnModel>               columnModels;
        List<? extends Model>           otherModels;
        RequestProcessor                rp = null;
        
        // Either the list contains 10 lists of individual models + one list of mixed models
        //  + optional TreeExpansionModelFilter(s) + optional RequestProcessor
        // ; or the models directly
        boolean hasLists = false;
        if (models.size() == 11 || models.size() == 12 || models.size() == 13) {
            Iterator it = models.iterator ();
            boolean failure = false;
            while (it.hasNext ()) {
                Object model = it.next();
                if (!(model instanceof List)) {
                    if (model instanceof RequestProcessor && !it.hasNext()) {
                        failure = false;
                    } else {
                        failure = true;
                    }
                    break;
                }
            }
            if (!it.hasNext() && !failure) { // All elements are lists
                hasLists = true;
            }
        }
        if (hasLists) { // We have 11 or 12 lists of individual models + optional RP
            treeModels =            (List<TreeModel>)       models.get(0);
            treeModelFilters =      (List<TreeModelFilter>) models.get(1);
            revertOrder(treeModelFilters);
            treeExpansionModels =   (List<TreeExpansionModel>) models.get(2);
            nodeModels =            (List<NodeModel>) models.get(3);
            nodeModelFilters =      (List<NodeModelFilter>) models.get(4);
            revertOrder(nodeModelFilters);
            tableModels =           (List<TableModel>) models.get(5);
            tableModelFilters =     (List<TableModelFilter>) models.get(6);
            revertOrder(tableModelFilters);
            nodeActionsProviders =  (List<NodeActionsProvider>) models.get(7);
            nodeActionsProviderFilters = (List<NodeActionsProviderFilter>) models.get(8);
            revertOrder(nodeActionsProviderFilters);
            columnModels =          (List<ColumnModel>) models.get(9);
            otherModels =           (List<? extends Model>) models.get(10);
            if (models.size() > 11) { // TreeExpansionModelFilter or RequestProcessor
                if (models.get(11) instanceof List) {
                    treeExpansionModelFilters = (List<TreeExpansionModelFilter>) models.get(11);
                } else {
                    rp = (RequestProcessor) models.get(11);
                    treeExpansionModelFilters = Collections.emptyList();
                }
            } else {
                treeExpansionModelFilters = Collections.emptyList();
            }
            //treeExpansionModelFilters = (models.size() > 11) ? (List<TreeExpansionModelFilter>) models.get(11) : (List<TreeExpansionModelFilter>) Collections.EMPTY_LIST;
            if (models.size() > 12) {
                rp = (RequestProcessor) models.get(12);
            }
        } else { // We have the models, need to find out what they implement
            treeModels =           new LinkedList<TreeModel> ();
            treeModelFilters =     new LinkedList<TreeModelFilter> ();
            treeExpansionModels =  new LinkedList<TreeExpansionModel> ();
            treeExpansionModelFilters = new LinkedList<TreeExpansionModelFilter> ();
            nodeModels =           new LinkedList<NodeModel> ();
            nodeModelFilters =     new LinkedList<NodeModelFilter> ();
            tableModels =          new LinkedList<TableModel> ();
            tableModelFilters =    new LinkedList<TableModelFilter> ();
            nodeActionsProviders = new LinkedList<NodeActionsProvider> ();
            nodeActionsProviderFilters = new LinkedList<NodeActionsProviderFilter> ();
            columnModels =         new LinkedList<ColumnModel> ();
            otherModels =          (List<? extends Model>) models;
        }
            
        Iterator it = otherModels.iterator ();
        while (it.hasNext ()) {
            Object model = it.next ();
            boolean first = model.getClass ().getName ().endsWith ("First");
            if (model instanceof TreeModel)
                treeModels.add((TreeModel) model);
            if (model instanceof TreeModelFilter)
                if (first)
                    treeModelFilters.add((TreeModelFilter) model);
                else
                    treeModelFilters.add(0, (TreeModelFilter) model);
            if (model instanceof TreeExpansionModel)
                treeExpansionModels.add((TreeExpansionModel) model);
            if (model instanceof TreeExpansionModelFilter)
                if (first)
                    treeExpansionModelFilters.add((TreeExpansionModelFilter) model);
                else
                    treeExpansionModelFilters.add(0, (TreeExpansionModelFilter) model);
            if (model instanceof NodeModel)
                nodeModels.add((NodeModel) model);
            if (model instanceof NodeModelFilter)
                if (first)
                    nodeModelFilters.add((NodeModelFilter) model);
                else
                    nodeModelFilters.add(0, (NodeModelFilter) model);
            if (model instanceof TableModel)
                tableModels.add((TableModel) model);
            if (model instanceof TableModelFilter)
                if (first)
                    tableModelFilters.add((TableModelFilter) model);
                else
                    tableModelFilters.add(0, (TableModelFilter) model);
            if (model instanceof NodeActionsProvider)
                nodeActionsProviders.add((NodeActionsProvider) model);
            if (model instanceof NodeActionsProviderFilter)
                if (first)
                    nodeActionsProviderFilters.add((NodeActionsProviderFilter) model);
                else
                    nodeActionsProviderFilters.add(0, (NodeActionsProviderFilter) model);
            if (model instanceof ColumnModel)
                columnModels.add((ColumnModel) model);
        }
        /*
        System.out.println("Tree Models = "+treeModels);
        System.out.println("Tree Model Filters = "+treeModelFilters);
        System.out.println("Tree Expans Models = "+treeExpansionModels);
        System.out.println("Node Models = "+nodeModels);
        System.out.println("Node Model Filters = "+nodeModelFilters);
        System.out.println("Table Models = "+tableModels);
        System.out.println("Table Model Filters = "+tableModelFilters);
        System.out.println("Node Action Providers = "+nodeActionsProviders);
        System.out.println("Node Action Provider Filters = "+nodeActionsProviderFilters);
        System.out.println("Column Models = "+columnModels);
         */
        if (treeModels.isEmpty ()) {
            TreeModel etm = new EmptyTreeModel();
            treeModels = Collections.singletonList(etm);
        }
        DefaultTreeExpansionModel defaultExpansionModel = null;
        if (treeExpansionModels.isEmpty()) {
            defaultExpansionModel = defaultExpansionModels.get(models);
            if (defaultExpansionModel != null) {
                defaultExpansionModel = defaultExpansionModel.cloneForNewModel();
            } else {
                defaultExpansionModel = new DefaultTreeExpansionModel();
            }
            defaultExpansionModels.put(models, defaultExpansionModel);
            treeExpansionModels = Collections.singletonList((TreeExpansionModel) defaultExpansionModel);
        }
        
        CompoundModel cm = new CompoundModel (
            createCompoundTreeModel (
                new DelegatingTreeModel (treeModels),
                treeModelFilters
            ),
            createCompoundTreeExpansionModel(
                new DelegatingTreeExpansionModel (treeExpansionModels),
                treeExpansionModelFilters
            ),
            createCompoundNodeModel (
                new DelegatingNodeModel (nodeModels),
                nodeModelFilters
            ),
            createCompoundNodeActionsProvider (
                new DelegatingNodeActionsProvider (nodeActionsProviders),
                nodeActionsProviderFilters
            ),
            columnModels,
            createCompoundTableModel (
                new DelegatingTableModel (tableModels),
                tableModelFilters
            ),
            propertiesHelpID,
            rp
        );
        if (defaultExpansionModel != null) {
            defaultExpansionModel.setCompoundModel(cm);
        }
        return cm;
    }
    
    private static <T> void revertOrder(List<T> filters) {
        int n = filters.size();
        // [TODO] do not remove the following line, prevents null to be returned by filters.remove(i);
        // needs deeper investigation why it can occure
        filters.toString();
        for (int i = 0; i < n; ) {
            T filter = filters.remove(i);
            boolean first = filter.getClass ().getName ().endsWith ("First");
            if (first) { // The "First" should be the last one in this list
                filters.add(filter);
                n--;
            } else {
                filters.add(0, filter);
                i++;
            }
        }
    }
    
    
    /**
     * Returns {@link javax.swing.Action} for given parameters.
     *
     * @param displayName a display name for action
     * @param performer a performer for action
     * @param multiselectionType The type of the multiselection - one of the
     *        MULTISELECTION_TYPE_* constants.
     *
     * @return a new instance of {@link javax.swing.Action} for given parameters
     */
    public static Action createAction (
        String displayName, 
        ActionPerformer performer,
        int multiselectionType
    ) {
        return new ActionSupport (
            displayName, 
            performer, 
            multiselectionType
        );
    }
    
    /**
     * Returns implementation of tree view features for given view.
     *
     * @param view a view created by this Models class
     * @throws UnsupportedOperationException in the case that given 
     *        view is not tree view
     * @return implementation of tree view features
     */
    public static TreeFeatures treeFeatures (JComponent view) 
    throws UnsupportedOperationException {
        return new DefaultTreeFeatures (view);
    }
    
    
    // private methods .........................................................
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
     * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
     * 
     * @param originalTreeModel a original tree model
     * @param treeModelFilter a list of tree model filters
     *
     * @returns compund tree model
     */
    private static TreeModel createCompoundTreeModel (
        TreeModel originalTreeModel,
        List treeModelFilters
    ) {
        TreeModel tm = originalTreeModel;
        int i, k = treeModelFilters.size ();
        for (i = 0; i < k; i++)
            tm = new CompoundTreeModel (
                tm,
                (TreeModelFilter) treeModelFilters.get (i)
            );
        return tm;
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.NodeModel} for given NodeModel and
     * {@link org.netbeans.spi.viewmodel.NodeModelFilter}.
     * 
     * @param originalNodeModel a original node model
     * @param nodeModelFilters a list of node model filters
     *
     * @returns compund tree model
     */
    private static ExtendedNodeModel createCompoundNodeModel (
        ExtendedNodeModel originalNodeModel,
        List treeNodeModelFilters
    ) {
        ExtendedNodeModel nm = originalNodeModel;
        int i, k = treeNodeModelFilters.size ();
        for (i = 0; i < k; i++)
            nm = new CompoundNodeModel (
                nm,
                (NodeModelFilter) treeNodeModelFilters.get (i)
            );
        return nm;
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
     * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
     * 
     * @param originalTableModel a original table model
     * @param tableModelFilters a list of table model filters
     *
     * @returns compund table model
     */
    private static TableModel createCompoundTableModel (
        TableModel originalTableModel,
        List tableModelFilters
    ) {
        TableModel tm = originalTableModel;
        int i, k = tableModelFilters.size ();
        for (i = 0; i < k; i++)
            tm = new CompoundTableModel (
                tm,
                (TableModelFilter) tableModelFilters.get (i)
            );
        return tm;
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.NodeActionsProvider} for given NodeActionsProvider and
     * {@link org.netbeans.spi.viewmodel.NodeActionsProviderFilter}.
     * 
     * @param originalNodeActionsProvider a original node actions provider
     * @param nodeActionsProviderFilters a list of node actions provider filters
     *
     * @returns compund node actions provider
     */
    private static NodeActionsProvider createCompoundNodeActionsProvider (
        NodeActionsProvider originalNodeActionsProvider,
        List nodeActionsProviderFilters
    ) {
        NodeActionsProvider nap = originalNodeActionsProvider;
        int i, k = nodeActionsProviderFilters.size ();
        for (i = 0; i < k; i++)
            nap = new CompoundNodeActionsProvider (
                nap,
                (NodeActionsProviderFilter) nodeActionsProviderFilters.get (i)
            );
        return nap;
    }
    
    private static TreeExpansionModel createCompoundTreeExpansionModel (
            TreeExpansionModel expansionModel,
            List<TreeExpansionModelFilter> filters
    ) {
        for (TreeExpansionModelFilter filter : filters) {
            expansionModel = new CompoundTreeExpansionModel (expansionModel, filter);
        }
        return expansionModel;
    }
    
    
    // innerclasses ............................................................
    
    /**
     * @author   Jan Jancura
     */
    private static class ActionSupport extends AbstractAction {

        private ActionPerformer     performer;
        private int                 multiselectionType;
        private String              displayName;

 
        ActionSupport (
            String displayName, 
            ActionPerformer performer,
            int multiselectionType
        ) {
            super (displayName);
            this.performer = performer;
            this.displayName = displayName;
            this.multiselectionType = multiselectionType;
        }
        
        @Override
        public boolean isEnabled () {
            boolean any = multiselectionType == MULTISELECTION_TYPE_ANY;
            Node[] ns = TopComponent.getRegistry ().getActivatedNodes ();
            if (multiselectionType == MULTISELECTION_TYPE_EXACTLY_ONE) {
                if (ns.length != 1) return false;
                return performer.isEnabled (
                    ns[0].getLookup().lookup(Object.class)
                );
            }
            int i, k = ns.length;
            if (k == 0) {
                if (!performer.isEnabled(TreeModel.ROOT)) {
                    return false;
                }
            } else {
                for (i = 0; i < k; i++)
                    if (!performer.isEnabled(ns[i].getLookup().lookup(Object.class))) {
                        if (!any) {
                            return false;
                        }
                    } else if (any) {
                        return true;
                    }
                if (any) return false;
            }
            return true;
        }

        public void actionPerformed (ActionEvent e) {
            //System.err.println("Models.ActionSupport.actionPerformed("+e+")");
            Node[] ns = TopComponent.getRegistry ().getActivatedNodes ();
            int i, k = ns.length;
            IdentityHashMap<Action, ArrayList<Object>> h = new IdentityHashMap<Action, ArrayList<Object>>();
            for (i = 0; i < k; i++) {
                Object node = ns[i].getLookup().lookup(Object.class);
                Action[] as = ns [i].getActions (false);
                int j, jj = as.length;
                for (j = 0; j < jj; j++)
                    if (equals (as [j])) {
                        ArrayList<Object> l = h.get (as [j]);
                        if (l == null) {
                            l = new ArrayList<Object>();
                            h.put (as [j], l);
                        }
                        l.add (node);
                    }
            }
            //System.err.println("  k = "+k);
            if (k == 0) {
                performer.perform(new Object[]{});
            } else {
                //System.err.println("  h = "+h);
                Iterator<Action> it = h.keySet ().iterator ();
                while (it.hasNext ()) {
                    ActionSupport a = (ActionSupport) it.next ();
                    //System.err.println("  "+a.performer+".perform("+((ArrayList) h.get (a)));
                    a.performer.perform (
                        ((ArrayList) h.get (a)).toArray ()
                    );
                }
            }
        }
        
        @Override
        public int hashCode () {
            return displayName.hashCode ();
        }
        
        @Override
        public boolean equals (Object o) {
            return (o instanceof ActionSupport) && 
                displayName.equals (((ActionSupport) o).displayName);
        }
    }

    /**
     * Support interface for 
     * {@link #createAction(String,Models.ActionPerformer,int)} method.
     */
    public static interface ActionPerformer {

        /**
         * Returns enabled property state for given node.
         *
         * @param node the node the action shouuld be applied to
         * @return enabled property state for given node
         *
         * @see #createAction(String,Models.ActionPerformer,int)
         */
        public boolean isEnabled (Object node);

        /**
         * Called when action <code>action</code> is performed for 
         * nodes.
         *
         * @param nodes nodes the action shouuld be applied to
         *
         * @see #createAction(String,Models.ActionPerformer,int)
         */
        public void perform (Object[] nodes);
    }

    /**
     * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
     * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
     * 
     * @author   Jan Jancura
     */
    private final static class CompoundTreeModel implements TreeModel, ModelListener {


        private TreeModel model;
        private TreeModelFilter filter;
        
        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();

        
        /**
         * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
         * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
         */
        CompoundTreeModel (TreeModel model, TreeModelFilter filter) {
            this.model = model;
            this.filter = filter;
        }

        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        public Object getRoot () {
            return filter.getRoot (model);
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @throws  NoInformationException if the set of children can not be 
         *          resolved
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         *
         * @return  children for given parent on given indexes
         */
        public Object[] getChildren (Object parent, int from, int to) 
            throws UnknownTypeException {

            return filter.getChildren (model, parent, from, to);
        }
    
        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  NoInformationException if the set of children can not be 
         *          resolved
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public int getChildrenCount (Object node) throws UnknownTypeException {
            return filter.getChildrenCount (model, node);
        }

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         * @return  true if node is leaf
         */
        public boolean isLeaf (Object node) throws UnknownTypeException {
            return filter.isLeaf (model, node);
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.size() == 0) {
                    filter.addModelListener (this);
                    model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                if (modelListeners.size() == 0) {
                    filter.removeModelListener (this);
                    model.removeModelListener (this);
                }
            }
        }

        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged &&
                    (event.getSource() instanceof NodeModel || event.getSource() instanceof NodeModelFilter)) {
                // CompoundNodeModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TableValueChanged &&
                    (event.getSource() instanceof TableModel || event.getSource() instanceof TableModelFilter)) {
                // CompoundTableModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            if (model instanceof CompoundTreeModel)
                return n + filter + "\n" +
                    ((CompoundTreeModel) model).toString (n + "  ");
            return n + filter + "\n" + 
                   n + "  " + model;
        }

    }
    
    private static ModelEvent translateEvent(ModelEvent event, Object newSource) {
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
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
     * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
     * 
     * @author   Jan Jancura
     */
    private final static class CompoundNodeModel implements ExtendedNodeModel, CheckNodeModel, ModelListener {


        private ExtendedNodeModel model;
        private NodeModelFilter filter;
        private CheckNodeModel cmodel;
        private CheckNodeModelFilter cfilter;

        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();


        /**
         * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
         * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
         */
        CompoundNodeModel (ExtendedNodeModel model, NodeModelFilter filter) {
            this.model = model;
            this.filter = filter;
            if (model instanceof CheckNodeModel) {
                this.cmodel = (CheckNodeModel) model;
            }
            if (filter instanceof CheckNodeModelFilter) {
                this.cfilter = (CheckNodeModelFilter) filter;
            }
        }
    
        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        public String getDisplayName (Object node) 
        throws UnknownTypeException {
            return filter.getDisplayName (model, node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            return filter.getIconBase (model, node);
        }

        /**
         * Returns tooltip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tooltip for given node type
         * @return  tooltip for given node
         */
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            return filter.getShortDescription (model, node);
        }


        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.size() == 0) {
                    filter.addModelListener (this);
                    model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                if (modelListeners.size() == 0) {
                    filter.removeModelListener (this);
                    model.removeModelListener (this);
                }
            }
        }

        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.TableValueChanged &&
                    (event.getSource() instanceof TableModel || event.getSource() instanceof TableModelFilter)) {
                // CompoundTableModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TreeChanged &&
                    (event.getSource() instanceof TreeModel || event.getSource() instanceof TreeModelFilter)) {
                // CompoundTreeModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            if (model instanceof CompoundNodeModel)
                return n + filter + "\n" +
                    ((CompoundNodeModel) model).toString (n + "  ");
            if (model instanceof DelegatingNodeModel)
                return n + filter + "\n" +
                    ((DelegatingNodeModel) model).toString (n + "  ");
            return n + filter + "\n" + 
                   n + "  " + model;
        }
    
        public boolean canRename(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).canRename(model, node);
            } else {
                return model.canRename(node);
            }
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).canCopy(model, node);
            } else {
                return model.canCopy(node);
            }
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).canCut(model, node);
            } else {
                return model.canCut(node);
            }
        }

        public Transferable clipboardCopy(Object node) throws IOException,
                                                              UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).clipboardCopy(model, node);
            } else {
                return model.clipboardCopy(node);
            }
        }

        public Transferable clipboardCut(Object node) throws IOException,
                                                             UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).clipboardCut(model, node);
            } else {
                return model.clipboardCut(node);
            }
        }

        /*public Transferable drag(Object node) throws IOException,
                                                     UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).drag(model, node);
            } else {
                return model.drag(node);
            }
        }*/

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).getPasteTypes(model, node, t);
            } else {
                return model.getPasteTypes(node, t);
            }
        }

        /*public PasteType getDropType(Object node, Transferable t, int action,
                                     int index) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).getDropType(model, node, t, action, index);
            } else {
                return model.getDropType(node, t, action, index);
            }
        }*/

        public void setName(Object node, String name) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                ((ExtendedNodeModelFilter) filter).setName(model, node, name);
            } else {
                model.setName(node, name);
            }
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).getIconBaseWithExtension(model, node);
            } else {
                String base;
                try {
                    base = filter.getIconBase(model, node);
                    if (base != null) {
                        base += ".gif";
                    }
                } catch (Exception utex) {
                    // The filter can not process the icon base filtering
                    // Perhaps it needs to be upgraded to ExtendedNodeModelFilter
                    Logger.getLogger(Models.class.getName()).log(Level.CONFIG,
                            "The filter "+filter+" does not perform icon base filtering for "+node+".\n"+
                            "If this is a problem, it should be upgraded to "+
                            "ExtendedNodeModelFilter and getIconBaseWithExtension() implemented.",
                            utex);
                    base = model.getIconBaseWithExtension(node);
                }
                return base;
            }
        }

        public boolean isCheckable(Object node) throws UnknownTypeException {
            if (cfilter != null) {
                return cfilter.isCheckable(model, node);
            } else if (cmodel != null) {
                return cmodel.isCheckable(node);
            } else {
                return false;
            }
        }

        public boolean isCheckEnabled(Object node) throws UnknownTypeException {
            if (cfilter != null) {
                return cfilter.isCheckEnabled(model, node);
            } else if (cmodel != null) {
                return cmodel.isCheckEnabled(node);
            } else {
                return true;
            }
        }

        public Boolean isSelected(Object node) throws UnknownTypeException {
            if (cfilter != null) {
                return cfilter.isSelected(model, node);
            } else if (cmodel != null) {
                return cmodel.isSelected(node);
            } else {
                return false;
            }
        }

        public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
            if (cfilter != null) {
                cfilter.setSelected(model, node, selected);
            } else if (cmodel != null) {
                cmodel.setSelected(node, selected);
            } else {
                Exceptions.printStackTrace(new IllegalStateException("Can not set selected state to model "+this));
            }
        }

    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
     * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
     * 
     * @author   Jan Jancura
     */
    private final static class CompoundTableModel implements TableModel, ModelListener {


        private TableModel model;
        private TableModelFilter filter;

        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();


        /**
         * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
         * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
         */
        CompoundTableModel (TableModel model, TableModelFilter filter) {
            this.model = model;
            this.filter = filter;
        }
    
        /**
         * Returns value to be displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return value of variable representing given position in tree table.
         */
        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            return filter.getValueAt (model, node, columnID);
        }

        /**
         * Returns true if value displayed in column <code>columnID</code>
         * and row <code>node</code> is read only. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return true if variable on given position is read only
         */
        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            return filter.isReadOnly (model, node, columnID);
        }

        /**
         * Changes a value displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @param value a new value of variable on given position
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         */
        public void setValueAt (Object node, String columnID, Object value) 
        throws UnknownTypeException {
            filter.setValueAt (model, node, columnID, value);
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.size() == 0) {
                    filter.addModelListener (this);
                    model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                if (modelListeners.size() == 0) {
                    filter.removeModelListener (this);
                    model.removeModelListener (this);
                }
            }
        }

        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged && (event.getSource() instanceof NodeModel || event.getSource() instanceof NodeModelFilter)) {
                // CompoundNodeModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TreeChanged &&
                    (event.getSource() instanceof TreeModel || event.getSource() instanceof TreeModelFilter)) {
                // CompoundTreeModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            if (model instanceof CompoundTableModel)
                return n + filter + "\n" +
                    ((CompoundTableModel) model).toString (n + "  ");
            if (model instanceof DelegatingTableModel)
                return n + filter + "\n" +
                    ((DelegatingTableModel) model).toString (n + "  ");
            return n + filter + "\n" + 
                   n + "  " + model;
        }
    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TreeModel}
     * from given list of TreeModels. DelegatingTreeModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private final static class DelegatingTreeModel implements TreeModel {

        private TreeModel[] models;
        private HashMap<String, TreeModel> classNameToModel = new HashMap<String, TreeModel>();


        /**
         * Creates new instance of DelegatingTreeModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTreeModel (List<TreeModel> models) {
            this (convert (models));
        }

        private static TreeModel[] convert (List<TreeModel> l) {
            TreeModel[] models = new TreeModel [l.size ()];
                return l.toArray (models);
            }

        /**
         * Creates new instance of DelegatingTreeModel for given array of 
         * TableModels.
         *
         * @param models a array of TreeModel
         */
        DelegatingTreeModel (TreeModel[] models) {
            this.models = models;        
        }
        
        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        public Object getRoot () {
            return models [0].getRoot ();
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @param   from a start index
         * @param   to a end index
         *
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  children for given parent on given indexes
         */
        public Object[] getChildren (Object node, int from, int to)
        throws UnknownTypeException {
            TreeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.getChildren (node, from, to);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    Object[] v = models [i].getChildren (node, from, to);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return v;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         * @since 1.1
         */
        public int getChildrenCount (Object node) 
        throws UnknownTypeException {
            TreeModel model = (TreeModel) classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.getChildrenCount (node);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    int result = models [i].getChildrenCount (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return result;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         * @return  true if node is leaf
         */
        public boolean isLeaf (Object node) throws UnknownTypeException {
            TreeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.isLeaf (node);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean result = models [i].isLeaf (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return result;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++)
                models [i].addModelListener (l);
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++)
                models [i].removeModelListener (l);
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) return "";
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.NodeActionsProvider} 
     * for given NodeActionsProvider and
     * {@link org.netbeans.spi.viewmodel.NodeActionsProviderFilter}.
     * 
     * @author   Jan Jancura
     */
    private final static class CompoundNodeActionsProvider 
    implements NodeActionsProvider {


        private NodeActionsProvider model;
        private NodeActionsProviderFilter filter;


        /**
         * Creates {@link org.netbeans.spi.viewmodel.NodeActionsProvider} 
         * for given NodeActionsProvider and
         * {@link org.netbeans.spi.viewmodel.NodeActionsProviderFilter}.
         */
        CompoundNodeActionsProvider (
            NodeActionsProvider model, 
            NodeActionsProviderFilter filter
        ) {
            this.model = model;
            this.filter = filter;
        }
    
        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider 
         *          implementation is not able to resolve actions 
         *          for given node type
         * @return  display name for given node
         */
        public void performDefaultAction (Object node) 
        throws UnknownTypeException {
            filter.performDefaultAction (model, node);
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public Action[] getActions (Object node) 
        throws UnknownTypeException {
            return filter.getActions (model, node);
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            if (model instanceof CompoundNodeActionsProvider)
                return n + filter + "\n" +
                    ((CompoundNodeActionsProvider) model).toString (n + "  ");
            if (model instanceof DelegatingNodeActionsProvider)
                return n + filter + "\n" +
                    ((DelegatingNodeActionsProvider) model).toString (n + "  ");
            return n + filter + "\n" + 
                   n + "  " + model;
        }
    }
    
    private final static class CompoundTreeExpansionModel implements TreeExpansionModel, ModelListener {
        
        private TreeExpansionModel expansionModel;
        private TreeExpansionModelFilter expansionFilter;
        
        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();
        
        CompoundTreeExpansionModel(TreeExpansionModel expansionModel, TreeExpansionModelFilter expansionFilter) {
            this.expansionModel = expansionModel;
            this.expansionFilter = expansionFilter;
        }

        public boolean isExpanded(Object node) throws UnknownTypeException {
            return expansionFilter.isExpanded(expansionModel, node);
        }

        public void nodeExpanded(Object node) {
            expansionModel.nodeExpanded(node);
            expansionFilter.nodeExpanded(node);
        }

        public void nodeCollapsed(Object node) {
            expansionModel.nodeCollapsed(node);
            expansionFilter.nodeCollapsed(node);
        }
        
        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.size() == 0) {
                    expansionFilter.addModelListener (this);
                    //model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                if (modelListeners.size() == 0) {
                    expansionFilter.removeModelListener (this);
                    //model.removeModelListener (this);
                }
            }
        }

        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged && (event.getSource() instanceof NodeModel || event.getSource() instanceof NodeModelFilter)) {
                // CompoundNodeModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TableValueChanged &&
                    (event.getSource() instanceof TableModel || event.getSource() instanceof TableModelFilter)) {
                // CompoundTableModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TableModel}
     * from given list of TableModels. DelegatingTableModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private final static class DelegatingTableModel implements TableModel {

        private TableModel[] models;
        private HashMap<String, TableModel> classNameToModel = new HashMap<String, TableModel>();


        /**
         * Creates new instance of DelegatingTableModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTableModel (List<TableModel> models) {
            this (convert (models));
        }

        private static TableModel[] convert (List<TableModel> l) {
            TableModel[] models = new TableModel [l.size ()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingTableModel for given array of 
         * TableModels.
         *
         * @param models a array of TableModels
         */
        DelegatingTableModel (TableModel[] models) {
            this.models = models;        
        }

        /**
         * Returns value to be displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return value of variable representing given position in tree table.
         */
        public Object getValueAt (Object node, String columnID)
        throws UnknownTypeException {
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.getValueAt (node, columnID);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    Object v = models [i].getValueAt (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return v;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    

        /**
         * Returns true if value displayed in column <code>columnID</code>
         * and row <code>node</code> is read only. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return true if variable on given position is read only
         */
        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.isReadOnly (node, columnID);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean ro = models [i].isReadOnly (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return ro;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /**
         * Changes a value displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @param value a new value of variable on given position
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         */
        public void setValueAt (Object node, String columnID, Object value)
        throws UnknownTypeException {
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    model.setValueAt (node, columnID, value);
                    return;
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    models [i].setValueAt (node, columnID, value);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++)
                models [i].addModelListener (l);
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++)
                models [i].removeModelListener (l);
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) return "";
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TableModel}
     * from given list of TableModels. DelegatingTableModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private final static class DelegatingTreeExpansionModel implements TreeExpansionModel {

        private TreeExpansionModel[] models;
        private HashMap<String, TreeExpansionModel> classNameToModel = new HashMap<String, TreeExpansionModel>();


        /**
         * Creates new instance of DelegatingTableModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTreeExpansionModel (List<TreeExpansionModel> models) {
            this (convert (models));
        }

        private static TreeExpansionModel[] convert (List<TreeExpansionModel> l) {
            TreeExpansionModel[] models = new TreeExpansionModel [l.size()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingTableModel for given array of 
         * TableModels.
         *
         * @param models a array of TableModels
         */
        private DelegatingTreeExpansionModel (TreeExpansionModel[] models) {
            this.models = models;        
        }

        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        public boolean isExpanded (Object node) 
        throws UnknownTypeException {
            TreeExpansionModel model = 
                classNameToModel.get (
                    node.getClass ().getName ()
                );
            if (model != null) 
                try {
                    return model.isExpanded (node);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean result = models [i].isExpanded (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return result;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    


        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        public void nodeExpanded (Object node) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].nodeExpanded (node);
            }
        }    

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        public void nodeCollapsed (Object node) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].nodeCollapsed (node);
            }
        }    

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) return "";
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }
    
    private static class DefaultTreeExpansionModel implements TreeExpansionModel {
        
        private Reference<CompoundModel> cmRef;
        private CompoundModel oldCM;
        
        public DefaultTreeExpansionModel() {
        }
        
        private DefaultTreeExpansionModel(CompoundModel oldCM) {
            this.oldCM = oldCM;
        }
        
        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        public boolean isExpanded (Object node) 
        throws UnknownTypeException {
            CompoundModel cm = cmRef.get();
            if (cm == null) return false;
            return DefaultTreeExpansionManager.get(cm).isExpanded(node);
        }

        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        public void nodeExpanded (Object node) {
            CompoundModel cm = cmRef.get();
            if (cm == null) return ;
            DefaultTreeExpansionManager.get(cm).setExpanded(node);
        }

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        public void nodeCollapsed (Object node) {
            CompoundModel cm = cmRef.get();
            if (cm == null) return ;
            DefaultTreeExpansionManager.get(cm).setCollapsed(node);
        }

        private void setCompoundModel(CompoundModel cm) {
            if (oldCM != null) {
                DefaultTreeExpansionManager.copyExpansions(oldCM, cm);
                oldCM = null;
            }
            cmRef = new WeakReference<CompoundModel>(cm);
        }
        
        private DefaultTreeExpansionModel cloneForNewModel() {
            return new DefaultTreeExpansionModel(cmRef.get());
        }

    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.NodeModel}
     * from given list of NodeModels. DelegatingNodeModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private static final class DelegatingNodeModel implements ExtendedNodeModel, CheckNodeModel {

        private NodeModel[] models;
        private HashMap<String, NodeModel> classNameToModel = new HashMap<String, NodeModel>();


        /**
         * Creates new instance of DelegatingNodeModel for given list of 
         * NodeModels.
         *
         * @param models a list of NodeModels
         */
        DelegatingNodeModel (
            List<NodeModel> models
        ) {
            this (convert (models));
        }

        private static NodeModel[] convert (List<NodeModel> l) {
            NodeModel[] models = new NodeModel [l.size ()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingNodeModel for given array of 
         * NodeModels.
         *
         * @param models a array of NodeModels
         */
        DelegatingNodeModel (
            NodeModel[] models
        ) {
            this.models = models;

        }
        
        NodeModel[] getModels() {
            return models;
        }

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        public String getDisplayName (Object node) 
        throws UnknownTypeException {
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.getDisplayName (node);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String dn = models [i].getDisplayName (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /**
         * Returns tooltip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tooltip for given node type
         * @return  tooltip for given node
         */
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.getShortDescription (node);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String dn = models [i].getShortDescription (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.getIconBase (node);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String dn = models [i].getIconBase (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++)
                models [i].addModelListener (l);
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++)
                models [i].removeModelListener (l);
        }

        @Override
        public String toString () {
            return toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) return "";
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
        
        // Extensions:
        
        private boolean defaultCanRename() {
            return false;
        }
        
        private boolean defaultCanCopy() {
            return false;
        }
    
        private boolean defaultCanCut() {
            return false;
        }
    
        private Transferable defaultClipboardCopy() throws IOException {
            return null;
        }

        private Transferable defaultClipboardCut() throws IOException {
            return null;
        }

        /*
        private Transferable defaultDrag() throws IOException {
            return null;
        }
         */

        private PasteType[] defaultGetPasteTypes(Transferable t) {
            return null;
        }

        /*
        private PasteType defaultGetDropType(Transferable t, int action,
                                            int index) {
            return null;
        }
         */

        private void defaultSetName(String name) {
            // nothing
        }

        public boolean canRename(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).canRename (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultCanRename();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        boolean cr = ((ExtendedNodeModel) models [i]).canRename (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return cr;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultCanRename();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).canCopy (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultCanCopy();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        boolean cr = ((ExtendedNodeModel) models [i]).canCopy (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return cr;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultCanCopy();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).canCut (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultCanCut();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        boolean cr = ((ExtendedNodeModel) models [i]).canCut (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return cr;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultCanCut();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        public Transferable clipboardCopy(Object node) throws IOException,
                                                              UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).clipboardCopy (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultClipboardCopy();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        Transferable t = ((ExtendedNodeModel) models [i]).clipboardCopy (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return t;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultClipboardCopy();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        public Transferable clipboardCut(Object node) throws IOException,
                                                             UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).clipboardCut (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultClipboardCut();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        Transferable t = ((ExtendedNodeModel) models [i]).clipboardCut (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return t;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultClipboardCut();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        /*
        public Transferable drag(Object node) throws IOException,
                                                     UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = (NodeModel) classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).drag (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultDrag();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        Transferable t = ((ExtendedNodeModel) models [i]).drag (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return t;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultDrag();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }
         */

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).getPasteTypes (node, t);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultGetPasteTypes(t);
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        PasteType[] p = ((ExtendedNodeModel) models [i]).getPasteTypes (node, t);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return p;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultGetPasteTypes(t);
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        /*
        public PasteType getDropType(Object node, Transferable t, int action,
                                     int index) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = (NodeModel) classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).getDropType (node, t, action, index);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultGetDropType(t, action, index);
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        PasteType p = ((ExtendedNodeModel) models [i]).getDropType (node, t, action, index);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return p;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultGetDropType(t, action, index);
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }
         */

        public void setName(Object node, String name) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        ((ExtendedNodeModel) model).setName (node, name);
                        return ;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    defaultSetName(name);
                    return ;
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        ((ExtendedNodeModel) models [i]).setName (node, name);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return ;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                defaultSetName(name);
                return ;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    if (model instanceof ExtendedNodeModel) {
                        return ((ExtendedNodeModel) model).getIconBaseWithExtension (node);
                    } else {
                        String base = model.getIconBase(node);
                        if (base != null) {
                            return base + ".gif";
                        } else {
                            return null;
                        }
                    }
                } catch (UnknownTypeException e) {
                    uex = e;
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String ib;
                    if (models[i] instanceof ExtendedNodeModel) {
                        ib = ((ExtendedNodeModel) models [i]).getIconBaseWithExtension (node);
                    } else {
                        String base = models[i].getIconBase(node);
                        if (base != null) {
                            ib = base + ".gif";
                        } else {
                            ib = null;
                        }
                    }
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return ib;
                } catch (UnknownTypeException e) {
                    uex = e;
                }
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
            
        }

        public boolean isCheckable(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            boolean isChecked = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        Boolean checkable = ((CheckNodeModel) models [i]).isCheckable(node);
                        return checkable;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isChecked = true;
                }
            }
            if (!isChecked) {
                return false;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        public boolean isCheckEnabled(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        boolean checkEnabled = ((CheckNodeModel) models [i]).isCheckEnabled(node);
                        return checkEnabled;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                }
            }
            if (uex != null) {
                throw uex;
            } else {
                return true;
            }
        }

        public Boolean isSelected(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            boolean isChecked = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        Boolean selected = ((CheckNodeModel) models [i]).isSelected(node);
                        return selected;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isChecked = true;
                }
            }
            if (!isChecked) {
                return false;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            boolean isChecked = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        ((CheckNodeModel) models [i]).setSelected (node, selected);
                        return ;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isChecked = true;
                }
            }
            if (!isChecked) {
                Exceptions.printStackTrace(new IllegalStateException("Can not set selected state to model "+this));
                return ;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

    }

    /**
     * Empty impleemntation of {@link org.netbeans.spi.viewmodel.TreeModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyTreeModel implements TreeModel {

        /** 
         * Returns {@link org.netbeans.spi.viewmodel.TreeModel#ROOT}.
         *
         * @return {@link org.netbeans.spi.viewmodel.TreeModel#ROOT}
         */
        public Object getRoot () {
            return ROOT;
        }

        /** 
         * Returns empty array.
         *
         * @return empty array
         */
        public Object[] getChildren (Object parent, int from, int to) {
            return new Object [0];
        }
    
        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public int getChildrenCount (Object node) {
            return 0;
        }

        /**
         * Returns false.
         *
         * @return false
         */
        public boolean isLeaf (Object node) {
            return false;
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be added
         */
        public void addModelListener (ModelListener l) {
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be removed
         */
        public void removeModelListener (ModelListener l) {
        }
    }

    /**
     * Empty impleemntation of {@link org.netbeans.spi.viewmodel.NodeModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyNodeModel implements NodeModel {

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        public String getDisplayName (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns tooltip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tooltip for given node type
         * @return  tooltip for given node
         */
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be added
         */
        public void addModelListener (ModelListener l) {
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be removed
         */
        public void removeModelListener (ModelListener l) {
        }
    }

    /**
     * Empty impleemntation of {@link org.netbeans.spi.viewmodel.TableModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyTableModel implements TableModel {
 
        /**
         * Returns value to be displayed in column <code>columnID</code>
         * and row identified by <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@link org.netbeans.spi.viewmodel.TreeModel#getChildren}.
         *
         * @param node a object returned from 
         *         {@link org.netbeans.spi.viewmodel.TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return value of variable representing given position in tree table.
         */
        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns true if value displayed in column <code>columnID</code>
         * and row <code>node</code> is read only. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@link TreeModel#getChildren}.
         *
         * @param node a object returned from {@link TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return true if variable on given position is read only
         */
        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Changes a value displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@link TreeModel#getChildren}.
         *
         * @param node a object returned from {@link TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @param value a new value of variable on given position
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         */
        public void setValueAt (Object node, String columnID, Object value) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }
        
        /** 
         * Do nothing.
         *
         * @param l the listener to be added
         */
        public void addModelListener (ModelListener l) {
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be removed
         */
        public void removeModelListener (ModelListener l) {
        }
    }

    /**
     * Empty impleemntation of {@link org.netbeans.spi.viewmodel.TableModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyNodeActionsProvider implements 
    NodeActionsProvider {
    
        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public void performDefaultAction (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public Action[] getActions (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }
    }
    
    /**
     * Creates one {@link org.netbeans.spi.viewmodel.NodeActionsProvider}
     * from given list of NodeActionsProviders. DelegatingNodeActionsProvider asks all 
     * underlaying models for each concrete parameter, and returns first 
     * returned value.
     *
     * @author   Jan Jancura
     */
    static final class DelegatingNodeActionsProvider implements NodeActionsProvider {

        private NodeActionsProvider[] models;
        private HashMap<String, NodeActionsProvider> classNameToModel = new HashMap<String, NodeActionsProvider>();


        /**
         * Creates new instance of DelegatingNodeActionsProvider for given list of 
         * NodeActionsProvider.
         *
         * @param models a list of NodeActionsProvider
         */
        public DelegatingNodeActionsProvider (
            List<NodeActionsProvider> models
        ) {
            this (convert (models));
        }

        private static NodeActionsProvider[] convert (List<NodeActionsProvider> l) {
            NodeActionsProvider[] models = new NodeActionsProvider [l.size ()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingNodeActionsProvider for given array of 
         * NodeActionsProvider.
         *
         * @param models a array of NodeActionsProvider
         */
        public DelegatingNodeActionsProvider (NodeActionsProvider[] models) {
            this.models = models;
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public Action[] getActions (Object node) 
        throws UnknownTypeException {
            NodeActionsProvider model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    return model.getActions (node);
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    Action[] dn = models [i].getActions (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            if (k == 0) {
                return new Action[] {};
            } else {
                throw new UnknownTypeException (node);
            }
        }

        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public void performDefaultAction (Object node) throws UnknownTypeException {
            NodeActionsProvider model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) 
                try {
                    model.performDefaultAction (node);
                    return;
                } catch (UnknownTypeException e) {
                }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    models [i].performDefaultAction (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) return "";
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }
    
    /**
     * Tree expansion control.
     * @since 1.15
     */
    public static abstract class TreeFeatures {
        
        /**
         * Returns <code>true</code> if given node is expanded.
         *
         * @param node a node to be checked
         * @return <code>true</code> if given node is expanded
         */
        public abstract boolean isExpanded (Object node);
        
        /**
         * Expands given list of nodes.
         *
         * @param node a list of nodes to be expanded
         */
        public abstract void expandNode (Object node);
        
        /**
         * Collapses given node.
         *
         * @param node a node to be expanded
         */
        public abstract void collapseNode (Object node);
        
    }
    
    /**
     * Implements set of tree view features.
     */
    private static final class DefaultTreeFeatures extends TreeFeatures {
        
        private JComponent view;
        
        private DefaultTreeFeatures (JComponent view) {
            this.view = view;
        }
        
        /**
         * Returns <code>true</code> if given node is expanded.
         *
         * @param node a node to be checked
         * @return <code>true</code> if given node is expanded
         */
        public boolean isExpanded (
            Object node
        ) {
            return ((OutlineTable) view).isExpanded (node);
        }

        /**
         * Expands given list of nodes.
         *
         * @param node a list of nodes to be expanded
         */
        public void expandNode (
            Object node
        ) {
            ((OutlineTable) view).expandNode (node);
        }

        /**
         * Collapses given node.
         *
         * @param node a node to be expanded
         */
        public void collapseNode (
            Object node
        ) {
            ((OutlineTable) view).collapseNode (node);
        }
    }

    /**
     * This model encapsulates all currently supported models.
     *
     * @see Models#createCompoundModel
     * @author   Jan Jancura
     */
    public static final class CompoundModel implements TreeModel, 
    ExtendedNodeModel, CheckNodeModel, NodeActionsProvider, TableModel, TreeExpansionModel {

        private TreeModel       treeModel;
        private ExtendedNodeModel nodeModel;
        private CheckNodeModel cnodeModel;
        private NodeActionsProvider nodeActionsProvider;
        private ColumnModel[]   columnModels;
        private TableModel      tableModel;
        private TreeExpansionModel treeExpansionModel;
        
        // <RAVE>
        // New field, setter/getter for propertiesHelpID, which is used
        // for property sheet help
        private String propertiesHelpID = null;
        // </RAVE>
        private RequestProcessor rp;
        
        // init ....................................................................

        /**
         * Creates a new instance of {@link CompoundModel} for given models.
         *
         * @param treeModel a tree model to delegate on
         * @param nodeModel a node model to delegate on
         * @param nodeActionsProvider a node actions provider to delegate on
         * @param nodeActionsProvider a columns modeol to delegate on
         */
        private CompoundModel (
            TreeModel treeModel, 
            TreeExpansionModel treeExpansionModel,
            ExtendedNodeModel nodeModel, 
            NodeActionsProvider nodeActionsProvider,
            List<ColumnModel> columnModels,
            TableModel tableModel,
            String propertiesHelpID,
            RequestProcessor rp
        ) {
            if (treeModel == null) throw new NullPointerException ();
            if (treeModel == null) throw new NullPointerException ();
            if (nodeModel == null) throw new NullPointerException ();
            if (tableModel == null) throw new NullPointerException ();
            if (nodeActionsProvider == null) throw new NullPointerException ();

            this.treeModel = treeModel;
            this.treeExpansionModel = treeExpansionModel;
            this.nodeModel = nodeModel;
            if (nodeModel instanceof CheckNodeModel) {
                this.cnodeModel = (CheckNodeModel) nodeModel;
            }
            this.tableModel = tableModel;
            this.nodeActionsProvider = nodeActionsProvider;
            this.columnModels = columnModels.toArray (
                new ColumnModel [columnModels.size ()]
            );
            this.propertiesHelpID = propertiesHelpID;
            this.rp = rp;
        }

        // <RAVE>
        /**
         * Get a help ID for this model.
         * @return The help ID defined for the properties sheets,
         *         or <code>null</code>.
         * @since 1.7
         */
        public String getHelpId() {
            return propertiesHelpID;
        }
        // </RAVE>

        // TreeModel ...............................................................

        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        public Object getRoot () {
            return treeModel.getRoot ();
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         *
         * @return  children for given parent on given indexes
         */
        public Object[] getChildren (Object parent, int from, int to) 
        throws UnknownTypeException {
            Object[] ch = treeModel.getChildren (parent, from, to);
            //System.err.println("Children for node '"+parent+"' are '"+java.util.Arrays.asList(ch)+"'");
            //System.err.println("Model = "+this);
            return ch;
        }

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public int getChildrenCount (Object node) throws UnknownTypeException {
            return treeModel.getChildrenCount (node);
        }

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         * @return  true if node is leaf
         */
        public boolean isLeaf (Object node) throws UnknownTypeException {
            return treeModel.isLeaf (node);
        }


        // NodeModel ...............................................................

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        public String getDisplayName (Object node) throws UnknownTypeException {
            if (nodeModel instanceof DelegatingNodeModel) {
                NodeModel[] subModels = ((DelegatingNodeModel) nodeModel).getModels();
                if (subModels.length == 0) {
                    if (TreeModel.ROOT.equals(node)) {
                        ColumnModel[] columns = getColumns();
                        for (ColumnModel cm : columns) {
                            if (cm.getType() == null) {
                                return Actions.cutAmpersand(cm.getDisplayName());
                            }
                        }
                    }
                    return ""; // Nothing when there are no models
                }
            }
            String dn = nodeModel.getDisplayName (node);
            //System.err.println("DisplayName for node '"+node+"' is: '"+dn+"'");
            //System.err.println("Model = "+this);
            return dn;
        }

        /**
         * Returns tooltip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tooltip for given node type
         * @return  tooltip for given node
         */
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            if (nodeModel instanceof DelegatingNodeModel) {
                NodeModel[] subModels = ((DelegatingNodeModel) nodeModel).getModels();
                if (subModels.length == 0) {
                    if (TreeModel.ROOT.equals(node)) {
                        ColumnModel[] columns = getColumns();
                        for (ColumnModel cm : columns) {
                            if (cm.getType() == null) {
                                return cm.getShortDescription();
                            }
                        }
                    }
                }
            }
            return nodeModel.getShortDescription (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            if (nodeModel instanceof DelegatingNodeModel) {
                NodeModel[] subModels = ((DelegatingNodeModel) nodeModel).getModels();
                if (subModels.length == 0) {
                    return null; // Nothing when there are no models
                }
            }
            String ib = nodeModel.getIconBase (node);
            //System.err.println("IconBase for node '"+node+"' is '"+ib+"'");
            //System.err.println("Model = "+this);
            return ib;
        }


        // NodeActionsProvider .....................................................

        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public void performDefaultAction (Object node) throws UnknownTypeException {
            nodeActionsProvider.performDefaultAction (node);
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public Action[] getActions (Object node) throws UnknownTypeException {
            return nodeActionsProvider.getActions (node);
        }


        // ColumnsModel ............................................................

        /**
         * Returns sorted array of 
         * {@link org.netbeans.spi.viewmodel.ColumnModel}s.
         *
         * @return sorted array of ColumnModels
         */
        public ColumnModel[] getColumns () {
            return columnModels;
        }


        // TableModel ..............................................................

        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            return tableModel.getValueAt (node, columnID);
        }

        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            return tableModel.isReadOnly (node, columnID);
        }

        public void setValueAt (Object node, String columnID, Object value) throws 
        UnknownTypeException {
            tableModel.setValueAt (node, columnID, value);
        }


        // TreeExpansionModel ......................................................

        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        public boolean isExpanded (Object node) throws UnknownTypeException {
            if (treeExpansionModel == null) return false;
            return treeExpansionModel.isExpanded (node);
        }

        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        public void nodeExpanded (Object node) {
            if (treeExpansionModel != null)
                treeExpansionModel.nodeExpanded (node);
        }

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        public void nodeCollapsed (Object node) {
            if (treeExpansionModel != null)
                treeExpansionModel.nodeCollapsed (node);
        }


        // listeners ...............................................................

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            treeModel.addModelListener (l);
            if (nodeModel != treeModel) {
                nodeModel.addModelListener (l);
            }
            if (tableModel != treeModel && tableModel != nodeModel) {
                tableModel.addModelListener (l);
            }
            if (treeExpansionModel instanceof CompoundTreeExpansionModel) {
                ((CompoundTreeExpansionModel) treeExpansionModel).addModelListener(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            treeModel.removeModelListener (l);
            if (nodeModel != treeModel) {
                nodeModel.removeModelListener (l);
            }
            if (tableModel != treeModel && tableModel != nodeModel) {
                tableModel.removeModelListener (l);
            }
            if (treeExpansionModel instanceof CompoundTreeExpansionModel) {
                ((CompoundTreeExpansionModel) treeExpansionModel).removeModelListener(l);
            }
        }

        @Override
        public String toString () {
            return super.toString () + 
                   "\n  TreeModel = " + treeModel +
                   "\n  NodeModel = " + nodeModel +
                   "\n  TableModel = " + tableModel +
                   "\n  NodeActionsProvider = " + nodeActionsProvider +
                   "\n  ColumnsModel = " + java.util.Arrays.asList(columnModels);
        }
        
        // ExtendedNodeModel
    
        public boolean canRename(Object node) throws UnknownTypeException {
            return nodeModel.canRename(node);
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            return nodeModel.canCopy(node);
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            return nodeModel.canCut(node);
        }

        public Transferable clipboardCopy(Object node) throws IOException,
                                                              UnknownTypeException {
            return nodeModel.clipboardCopy(node);
        }

        public Transferable clipboardCut(Object node) throws IOException,
                                                             UnknownTypeException {
            return nodeModel.clipboardCut(node);
        }

        /*
        public Transferable drag(Object node) throws IOException,
                                                     UnknownTypeException {
            return nodeModel.drag(node);
        }
         */

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            return nodeModel.getPasteTypes(node, t);
        }

        /*
        public PasteType getDropType(Object node, Transferable t, int action,
                                     int index) throws UnknownTypeException {
            return nodeModel.getDropType(node, t, action, index);
        }
         */

        public void setName(Object node, String name) throws UnknownTypeException {
            nodeModel.setName(node, name);
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            String ib = nodeModel.getIconBaseWithExtension(node);
            //System.err.println("IconBase for node '"+node+"' is '"+ib+"'");
            //System.err.println("Model = "+this);
            return ib;
        }

        public boolean isCheckable(Object node) throws UnknownTypeException {
            if (cnodeModel != null) {
                return cnodeModel.isCheckable(node);
            } else {
                return false;
            }
        }

        public boolean isCheckEnabled(Object node) throws UnknownTypeException {
            if (cnodeModel != null) {
                return cnodeModel.isCheckEnabled(node);
            } else {
                return true;
            }
        }

        public Boolean isSelected(Object node) throws UnknownTypeException {
            if (cnodeModel != null) {
                return cnodeModel.isSelected(node);
            } else {
                return false;
            }
        }

        public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
            if (cnodeModel != null) {
                cnodeModel.setSelected(node, selected);
            } else {
                Exceptions.printStackTrace(new IllegalStateException("Can not set selected state to model "+nodeModel));
            }
        }

    }
}
