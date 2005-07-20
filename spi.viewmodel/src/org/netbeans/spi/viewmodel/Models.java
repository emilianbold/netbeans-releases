/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.viewmodel;

import java.awt.event.ActionEvent;
import java.lang.StringBuffer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.netbeans.modules.viewmodel.TreeModelNode;
import org.netbeans.modules.viewmodel.TreeTable;

import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.nodes.Node;
import org.openide.util.WeakSet;

import org.openide.windows.TopComponent;


/**
 * Contains various utility methods for various models.
 *
 * @author   Jan Jancura
 */
public final class Models {

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
        TreeTable tt = new TreeTable ();
        tt.setModel (compoundModel);
        return tt;
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
        if (!(view instanceof TreeTable)) {
            throw new IllegalArgumentException("Expecting an instance of "+TreeTable.class.getName()+", which can be obtained from Models.createView().");
        }
        if (verbose)
            System.out.println (compoundModel);
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                ((TreeTable) view).setModel (compoundModel);
            }
        });
    }
    
    /**
     * Creates one {@link CompoundModel} from given list of models.
     * 
     * @param models a list of models
     * @return {@link CompoundModel} encapsulating given list of models
     */
    public static CompoundModel createCompoundModel (List models) {
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
        List otherModels;
        
        // Either the list contains 10 lists of individual models + one list of mixed models; or the models directly
        boolean hasLists = false;
        if (models.size() == 11) {
            Iterator it = models.iterator ();
            while (it.hasNext ()) {
                if (!(it.next() instanceof List)) break;
            }
            if (!it.hasNext()) { // All elements are lists
                hasLists = true;
            }
        }
        if (hasLists) { // We have 11 lists of individual models
            treeModels =            (List) models.get(0);
            treeModelFilters =      (List) models.get(1);
            revertOrder(treeModelFilters);
            treeExpansionModels =   (List) models.get(2);
            nodeModels =            (List) models.get(3);
            nodeModelFilters =      (List) models.get(4);
            revertOrder(nodeModelFilters);
            tableModels =           (List) models.get(5);
            tableModelFilters =     (List) models.get(6);
            revertOrder(tableModelFilters);
            nodeActionsProviders =  (List) models.get(7);
            nodeActionsProviderFilters = (List) models.get(8);
            revertOrder(nodeActionsProviderFilters);
            columnModels =          (List) models.get(9);
            otherModels =           (List) models.get(10);
        } else { // We have the models, need to find out what they implement
            treeModels =           new LinkedList ();
            treeModelFilters =     new LinkedList ();
            treeExpansionModels =  new LinkedList ();
            nodeModels =           new LinkedList ();
            nodeModelFilters =     new LinkedList ();
            tableModels =          new LinkedList ();
            tableModelFilters =    new LinkedList ();
            nodeActionsProviders = new LinkedList ();
            nodeActionsProviderFilters = new LinkedList ();
            columnModels =         new LinkedList ();
            otherModels =          models;
        }
            
        Iterator it = otherModels.iterator ();
        while (it.hasNext ()) {
            Object model = it.next ();
            boolean first = model.getClass ().getName ().endsWith ("First");
            if (model instanceof TreeModel)
                treeModels.add(model);
            if (model instanceof TreeModelFilter)
                if (first)
                    treeModelFilters.add(model);
                else
                    treeModelFilters.add(0, model);
            if (model instanceof TreeExpansionModel)
                treeExpansionModels.add(model);
            if (model instanceof NodeModel)
                nodeModels.add(model);
            if (model instanceof NodeModelFilter)
                if (first)
                    nodeModelFilters.add(model);
                else
                    nodeModelFilters.add(0, model);
            if (model instanceof TableModel)
                tableModels.add(model);
            if (model instanceof TableModelFilter)
                if (first)
                    tableModelFilters.add(model);
                else
                    tableModelFilters.add(0, model);
            if (model instanceof NodeActionsProvider)
                nodeActionsProviders.add(model);
            if (model instanceof NodeActionsProviderFilter)
                if (first)
                    nodeActionsProviderFilters.add(model);
                else
                    nodeActionsProviderFilters.add(0, model);
            if (model instanceof ColumnModel)
                columnModels.add(model);
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
            treeModels.add (new EmptyTreeModel ());
        }
        
        return new CompoundModel (
            createCompoundTreeModel (
                new DelegatingTreeModel (treeModels),
                treeModelFilters
            ),
            new DelegatingTreeExpansionModel (treeExpansionModels),
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
            )
        );
    }
    
    private static void revertOrder(List filters) {
        int n = filters.size();
        for (int i = 0; i < n; ) {
            Object filter = filters.remove(i);
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
        return new TreeFeatures (view);
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
    private static NodeModel createCompoundNodeModel (
        NodeModel originalNodeModel,
        List treeNodeModelFilters
    ) {
        NodeModel nm = originalNodeModel;
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
        
        public boolean isEnabled () {
            if (multiselectionType == MULTISELECTION_TYPE_ANY)
                return true;
            Node[] ns = TopComponent.getRegistry ().getActivatedNodes ();
            if (multiselectionType == MULTISELECTION_TYPE_EXACTLY_ONE) {
                if (ns.length != 1) return false;
                return performer.isEnabled (
                    ((TreeModelNode) ns [0]).getObject ()
                );
            }
            int i, k = ns.length;
            for (i = 0; i < k; i++)
                if (!performer.isEnabled (
                    ((TreeModelNode) ns [i]).getObject ()
                 )) return false;
            return true;
        }

        public void actionPerformed (ActionEvent e) {
            Node[] ns = TopComponent.getRegistry ().getActivatedNodes ();
            int i, k = ns.length;
            IdentityHashMap h = new IdentityHashMap ();
            for (i = 0; i < k; i++) {
                Object node = ((TreeModelNode) ns [i]).getObject ();
                Action[] as = ns [i].getActions (false);
                int j, jj = as.length;
                for (j = 0; j < jj; j++)
                    if (equals (as [j])) {
                        ArrayList l = (ArrayList) h.get (as [j]);
                        if (l == null) {
                            l = new ArrayList ();
                            h.put (as [j], l);
                        }
                        l.add (node);
                    }
            }
            Iterator it = h.keySet ().iterator ();
            while (it.hasNext ()) {
                ActionSupport a = (ActionSupport) it.next ();
                a.performer.perform (
                    ((ArrayList) h.get (a)).toArray ()
                );
            }
        }
        
        public int hashCode () {
            return displayName.hashCode ();
        }
        
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
    final static class CompoundTreeModel implements TreeModel, ModelListener {


        private TreeModel model;
        private TreeModelFilter filter;
        
        private Collection modelListeners = new HashSet();

        
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
            ModelEvent newEvent = translateEvent(event, this);
            Collection listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList(modelListeners);
            }
            for (Iterator it = listeners.iterator(); it.hasNext(); ) {
                ((ModelListener) it.next()).modelChanged(newEvent);
            }
        }
        
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
            if (event instanceof javax.naming.ldap.ExtendedResponse) {
                newEvent = new NodeChangedEvent(newSource,
                        ((ModelEvent.NodeChanged) event).getNode(),
                        ((javax.naming.ldap.ExtendedResponse) event).getID());
            } else {
                newEvent = new ModelEvent.NodeChanged(newSource,
                        ((ModelEvent.NodeChanged) event).getNode());
            }
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
    
    private static class NodeChangedEvent extends ModelEvent.NodeChanged
                                          implements javax.naming.ldap.ExtendedResponse {
        
        private String ID;
        
        public NodeChangedEvent(Object source, Object node, String ID) {
            super(source, node);
            this.ID = ID;
        }
        
        public byte[] getEncodedValue() {
            return null;
        }

        public String getID() {
            return ID;
        }
        
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
     * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
     * 
     * @author   Jan Jancura
     */
    final static class CompoundNodeModel implements NodeModel, ModelListener {


        private NodeModel model;
        private NodeModelFilter filter;

        private Collection modelListeners = new HashSet();


        /**
         * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
         * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
         */
        CompoundNodeModel (NodeModel model, NodeModelFilter filter) {
            this.model = model;
            this.filter = filter;
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
            ModelEvent newEvent = translateEvent(event, this);
            Collection listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList(modelListeners);
            }
            for (Iterator it = listeners.iterator(); it.hasNext(); ) {
                ((ModelListener) it.next()).modelChanged(newEvent);
            }
        }
        
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
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
     * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
     * 
     * @author   Jan Jancura
     */
    final static class CompoundTableModel implements TableModel, ModelListener {


        private TableModel model;
        private TableModelFilter filter;

        private Collection modelListeners = new HashSet();


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
            ModelEvent newEvent = translateEvent(event, this);
            Collection listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList(modelListeners);
            }
            for (Iterator it = listeners.iterator(); it.hasNext(); ) {
                ((ModelListener) it.next()).modelChanged(newEvent);
            }
        }
        
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
    final static class DelegatingTreeModel implements TreeModel {

        private TreeModel[] models;
        private HashMap classNameToModel = new HashMap ();


        /**
         * Creates new instance of DelegatingTreeModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTreeModel (List models) {
            this (convert (models));
        }

        private static TreeModel[] convert (List l) {
            TreeModel[] models = new TreeModel [l.size ()];
            return (TreeModel[]) l.toArray (models);
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
            TreeModel model = (TreeModel) classNameToModel.get (
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
            TreeModel model = (TreeModel) classNameToModel.get (
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
    final static class CompoundNodeActionsProvider 
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

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TableModel}
     * from given list of TableModels. DelegatingTableModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    final static class DelegatingTableModel implements TableModel {

        private TableModel[] models;
        private HashMap classNameToModel = new HashMap ();


        /**
         * Creates new instance of DelegatingTableModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTableModel (List models) {
            this (convert (models));
        }

        private static TableModel[] convert (List l) {
            TableModel[] models = new TableModel [l.size ()];
            return (TableModel[]) l.toArray (models);
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
            TableModel model = (TableModel) classNameToModel.get (
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
            TableModel model = (TableModel) classNameToModel.get (
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
            TableModel model = (TableModel) classNameToModel.get (
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
    final static class DelegatingTreeExpansionModel 
    implements TreeExpansionModel {

        private TreeExpansionModel[] models;
        private HashMap classNameToModel = new HashMap ();


        /**
         * Creates new instance of DelegatingTableModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTreeExpansionModel (List models) {
            this (convert (models));
        }

        private static TreeExpansionModel[] convert (List l) {
            int size = l.size ();
            if (size == 0) {
                return new TreeExpansionModel[] {
                    new DefaultTreeExpansionModel()
                };
            } else {
                TreeExpansionModel[] models = new TreeExpansionModel [size];
                return (TreeExpansionModel[]) l.toArray (models);
            }
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
            TreeExpansionModel model = (TreeExpansionModel) 
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
        
        private Set expandedNodes = new WeakSet();
        private Set collapsedNodes = new WeakSet();

        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        public boolean isExpanded (Object node) 
        throws UnknownTypeException {
            synchronized (this) {
                if (expandedNodes.contains(node)) {
                    return true;
                }
                if (collapsedNodes.contains(node)) {
                    return false;
                }
            }
            // Default behavior follows:
            return false;
        }

        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        public void nodeExpanded (Object node) {
            synchronized (this) {
                expandedNodes.add(node);
                collapsedNodes.remove(node);
            }
        }

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        public void nodeCollapsed (Object node) {
            synchronized (this) {
                collapsedNodes.add(node);
                expandedNodes.remove(node);
            }
        }
        
    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.NodeModel}
     * from given list of NodeModels. DelegatingNodeModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    static final class DelegatingNodeModel implements NodeModel {

        private NodeModel[] models;
        private HashMap classNameToModel = new HashMap ();


        /**
         * Creates new instance of DelegatingNodeModel for given list of 
         * NodeModels.
         *
         * @param models a list of NodeModels
         */
        DelegatingNodeModel (
            List models
        ) {
            this (convert (models));
        }

        private static NodeModel[] convert (List l) {
            NodeModel[] models = new NodeModel [l.size ()];
            return (NodeModel[]) l.toArray (models);
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
            NodeModel model = (NodeModel) classNameToModel.get (
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
            NodeModel model = (NodeModel) classNameToModel.get (
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
            NodeModel model = (NodeModel) classNameToModel.get (
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
        private HashMap classNameToModel = new HashMap ();


        /**
         * Creates new instance of DelegatingNodeActionsProvider for given list of 
         * NodeActionsProvider.
         *
         * @param models a list of NodeActionsProvider
         */
        public DelegatingNodeActionsProvider (
            List models
        ) {
            this (convert (models));
        }

        private static NodeActionsProvider[] convert (List l) {
            NodeActionsProvider[] models = new NodeActionsProvider [l.size ()];
            return (NodeActionsProvider[]) l.toArray (models);
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
            NodeActionsProvider model = (NodeActionsProvider) classNameToModel.get (
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
            throw new UnknownTypeException (node);
        }

        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public void performDefaultAction (Object node) throws UnknownTypeException {
            NodeActionsProvider model = (NodeActionsProvider) classNameToModel.get (
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
     * Implements set of tree view features.
     */
    public static final class TreeFeatures {
        
        private JComponent view;
        
        private TreeFeatures (JComponent view) {
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
            return ((TreeTable) view).isExpanded (node);
        }

        /**
         * Expands given list of nodes.
         *
         * @param node a list of nodes to be expanded
         */
        public void expandNode (
            Object node
        ) {
            ((TreeTable) view).expandNode (node);
        }

        /**
         * Collapses given node.
         *
         * @param node a node to be expanded
         */
        public void collapseNode (
            Object node
        ) {
            ((TreeTable) view).collapseNode (node);
        }
    }

    /**
     * This model encapsulates all currently supported models.
     *
     * @see Models#createCompoundModel
     * @author   Jan Jancura
     */
    public static final class CompoundModel implements TreeModel, 
    NodeModel, NodeActionsProvider, TableModel, TreeExpansionModel {

        private TreeModel       treeModel;
        private NodeModel       nodeModel;
        private NodeActionsProvider nodeActionsProvider;
        private ColumnModel[]   columnModels;
        private TableModel      tableModel;
        private TreeExpansionModel treeExpansionModel;


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
            NodeModel nodeModel, 
            NodeActionsProvider nodeActionsProvider,
            List columnModels,
            TableModel tableModel
        ) {
            if (treeModel == null) throw new NullPointerException ();
            if (treeModel == null) throw new NullPointerException ();
            if (nodeModel == null) throw new NullPointerException ();
            if (tableModel == null) throw new NullPointerException ();
            if (nodeActionsProvider == null) throw new NullPointerException ();

            this.treeModel = treeModel;
            this.treeExpansionModel = treeExpansionModel;
            this.nodeModel = nodeModel;
            this.tableModel = tableModel;
            this.nodeActionsProvider = nodeActionsProvider;
            this.columnModels = (ColumnModel[]) columnModels.toArray (
                new ColumnModel [columnModels.size ()]
            );
        }


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
            return treeModel.getChildren (parent, from, to);
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
                    return ""; // Nothing when there are no models
                }
            }
            return nodeModel.getDisplayName (node);
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
            return nodeModel.getIconBase (node);
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
            nodeModel.addModelListener (l);
            tableModel.addModelListener (l);
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            treeModel.removeModelListener (l);
            nodeModel.removeModelListener (l);
            tableModel.removeModelListener (l);
        }

        public String toString () {
            return super.toString () + 
                   "\n  TreeModel = " + treeModel +
                   "\n  NodeModel = " + nodeModel +
                   "\n  TableModel = " + tableModel +
                   "\n  NodeActionsProvider = " + nodeActionsProvider +
                   "\n  ColumnsModel = " + java.util.Arrays.asList(columnModels);
        }
    }
}
