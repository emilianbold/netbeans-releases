/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class SourcesModel implements TreeModel, TableModel,
NodeActionsProvider {
    
    private Listener listener;
    private SourcePath context;
    private JPDADebugger debugger;
    private Vector listeners = new Vector ();
    private Set filters = new HashSet ();
    private Set enabledFilters = new HashSet ();
    private String FILTER_PREFIX = "Do not stop in: ";
    
    
    public SourcesModel (ContextProvider lookupProvider) {
         context = (SourcePath) lookupProvider.
            lookupFirst (null, SourcePath.class);
         debugger = (JPDADebugger) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    
    // TreeModel ...............................................................
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to)
    throws UnknownTypeException {
        if (parent == ROOT) {
            String[] sr = context.getOriginalSourceRoots ();
            String[] ep = new String [filters.size ()];
            ep = (String[]) filters.toArray (ep);
            int i, k = ep.length;
            for (i = 0; i < k; i++) {
                ep [i] = FILTER_PREFIX + ep [i];
            }
            Object[] os = new Object [sr.length + ep.length];
            System.arraycopy (sr, 0, os, 0, sr.length);
            System.arraycopy (ep, 0, os, sr.length, ep.length);
            Object[] fos = new Object [to - from];
            System.arraycopy (os, from, fos, 0, to - from);
            if (listener == null)
                listener = new Listener (this);
            return fos;
        } else
        throw new UnknownTypeException (parent);
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
        if (node == ROOT) {
            if (listener == null)
                listener = new Listener (this);
            return context.getOriginalSourceRoots ().length + 
                filters.size ();
        } else
        throw new UnknownTypeException (node);
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof String) return true;
        throw new UnknownTypeException (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    public void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
    
    public void fireNodeChanged (Breakpoint b) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeNodeChanged (b);
    }
    
    
    // ColumnModels ............................................................
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class DefaultSourcesColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultSourcesColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultSourcesColumn.class).
                getString ("CTL_SourcesModel_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultSourcesColumn.class).getString
                ("CTL_SourcesModel_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class SourcesUsedColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "use";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (SourcesModel.class).getString 
                ("CTL_SourcesModel_Column_Debugging_Name");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Boolean.TYPE;
        }

        /**
         * Returns tooltip for given column. Default implementation returns 
         * <code>null</code> - do not use tooltip.
         *
         * @return  tooltip for given node or <code>null</code>
         */
        public String getShortDescription () {
            return NbBundle.getBundle (SourcesModel.class).getString 
                ("CTL_SourcesModel_Column_Debugging_Desc");
        }

        /**
         * True if column should be visible by default. Default implementation 
         * returns <code>true</code>.
         *
         * @return <code>true</code> if column should be visible by default
         */
        public boolean initiallyVisible () {
            return true;
        }
    }
    
     
    // TableModel ..............................................................
    
    public Object getValueAt (Object node, String columnID) throws 
    ComputingException, UnknownTypeException {
        if (columnID.equals ("use")) {
            if (node instanceof String)
                return new Boolean (
                    isEnabled ((String) node)
                );
        } 
        throw new UnknownTypeException (node);
    }
    
    public boolean isReadOnly (Object node, String columnID) throws 
    UnknownTypeException {
        if ( columnID.equals ("use") &&
             (node instanceof String))
            return false;
        throw new UnknownTypeException (node);
    }
    
    public void setValueAt (Object node, String columnID, Object value) 
    throws UnknownTypeException {
        if (columnID.equals ("use")) {
            if (node instanceof String) {
                setEnabled ((String) node, ((Boolean) value).booleanValue ());
                return;
            }
        } 
        throw new UnknownTypeException (node);
    }
    
    
    // NodeActionsProvider .....................................................
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node instanceof String) {
            if (((String) node).startsWith (FILTER_PREFIX))
                return new Action[] {
                    NEW_FILTER_ACTION,
                    DELETE_ACTION
                };
            else
                return new Action[] {
                    NEW_FILTER_ACTION
                };
        } else
        throw new UnknownTypeException (node);
    }    
    
    public void performDefaultAction (Object node) 
    throws UnknownTypeException {
        if (node instanceof String) {
            return;
        } else
        throw new UnknownTypeException (node);
    }
    
    // other methods ...........................................................
    
    private boolean isEnabled (String root) {
        if (root.startsWith (FILTER_PREFIX)) {
            return enabledFilters.contains (root.substring (
                FILTER_PREFIX.length ()
            ));
        }
        String[] sr = context.getSourceRoots ();
        int i, k = sr.length;
        for (i = 0; i < k; i++)
            if (sr [i].equals (root)) return true;
        return false;
    }

    private void setEnabled (String root, boolean enabled) {
        if (root.startsWith (FILTER_PREFIX)) {
            String filter = root.substring (FILTER_PREFIX.length ());
            if (enabled) {
                enabledFilters.add  (filter);
                debugger.getSmartSteppingFilter ().addExclusionPatterns (
                        Collections.singleton (filter)
                );
            } else {
                enabledFilters.remove (filter);
                debugger.getSmartSteppingFilter ().removeExclusionPatterns (
                        Collections.singleton (filter)
                );
            }
            return;
        }
        String[] sr = context.getSourceRoots ();
        Set s = new HashSet (Arrays.asList (sr));
        if (enabled)
            s.add (root);
        else
            s.remove (root);
        String[] ss = new String [s.size ()];
        context.setSourceRoots ((String[]) s.toArray (ss));
    }

    
    // innerclasses ............................................................
    
    private final Action NEW_FILTER_ACTION = new AbstractAction
        (NbBundle.getBundle(SourcesModel.class).getString("CTL_SourcesModel_Action_AddFilter")) {
            public void actionPerformed (ActionEvent e) {
                NotifyDescriptor.InputLine descriptor = new NotifyDescriptor.InputLine (
                    NbBundle.getBundle(SourcesModel.class).getString("CTL_SourcesModel_NewFilter_Filter_Label"),
                    NbBundle.getBundle(SourcesModel.class).getString("CTL_SourcesModel_NewFilter_Title")
                );
                if (DialogDisplayer.getDefault ().notify (descriptor) == 
                    NotifyDescriptor.OK_OPTION
                ) {
                    String filter = descriptor.getInputText ();
                    filters.add (filter);
                    enabledFilters.add (filter);
                    debugger.getSmartSteppingFilter ().addExclusionPatterns (
                        Collections.singleton (filter)
                    );
                    fireTreeChanged ();
                }
            }
    };
    private final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle(SourcesModel.class).getString("CTL_SourcesModel_Action_Delete"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    filters.remove (((String) nodes [i]).substring (
                        FILTER_PREFIX.length ()
                    ));
                fireTreeChanged ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference model;
        
        private Listener (
            SourcesModel tm
        ) {
            model = new WeakReference (tm);
            tm.context.addPropertyChangeListener (this);
            tm.debugger.getSmartSteppingFilter ().
                addPropertyChangeListener (this);
        }
        
        private SourcesModel getModel () {
            SourcesModel tm = (SourcesModel) model.get ();
            if (tm == null) {
                tm.context.removePropertyChangeListener (this);
                tm.debugger.getSmartSteppingFilter ().
                    removePropertyChangeListener (this);
            }
            return tm;
        }
    
        public void propertyChange (PropertyChangeEvent evt) {
            SourcesModel m = getModel ();
            if (m == null) return;
            m.fireTreeChanged ();
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public abstract static class AbstractColumn extends ColumnModel {
        
        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views");

        
        /**
         * Set true if column is visible.
         *
         * @param visible set true if column is visible
         */
        public void setVisible (boolean visible) {
            properties.setBoolean (getID () + ".visible", visible);
        }

        /**
         * Set true if column should be sorted by default.
         *
         * @param sorted set true if column should be sorted by default 
         */
        public void setSorted (boolean sorted) {
            properties.setBoolean (getID () + ".sorted", sorted);
        }

        /**
         * Set true if column should be sorted by default in descending order.
         *
         * @param sortedDescending set true if column should be sorted by default 
         *        in descending order
         */
        public void setSortedDescending (boolean sortedDescending) {
            properties.setBoolean (getID () + ".sortedDescending", sortedDescending);
        }
    
        /**
         * Should return current order number of this column.
         *
         * @return current order number of this column
         */
        public int getCurrentOrderNumber () {
            return properties.getInt (getID () + ".currentOrderNumber", -1);
        }

        /**
         * Is called when current order number of this column is changed.
         *
         * @param newOrderNumber new order number
         */
        public void setCurrentOrderNumber (int newOrderNumber) {
            properties.setInt (getID () + ".currentOrderNumber", newOrderNumber);
        }

        /**
         * Return column width of this column.
         *
         * @return column width of this column
         */
        public int getColumnWidth () {
            return properties.getInt (getID () + ".columnWidth", 150);
        }

        /**
         * Is called when column width of this column is changed.
         *
         * @param newColumnWidth a new column width
         */
        public void setColumnWidth (int newColumnWidth) {
            properties.setInt (getID () + ".columnWidth", newColumnWidth);
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean isSorted () {
            return properties.getBoolean (getID () + ".sorted", false);
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean isSortedDescending () {
            return properties.getBoolean (getID () + ".sortedDescending", false);
        }
    }
}
