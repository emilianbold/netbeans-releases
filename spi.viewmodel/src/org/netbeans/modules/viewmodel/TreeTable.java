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

package org.netbeans.modules.viewmodel;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.spi.viewmodel.Models;

import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.windows.TopComponent;


/**
 * Implements root node of hierarchy created for given TreeModel.
 *
 * @author   Jan Jancura
 */
public class TreeTable extends JPanel implements 
ExplorerManager.Provider, PropertyChangeListener {
    
    private ExplorerManager explorerManager;
    private MyTreeTable treeTable;
    
    
    public TreeTable () {
        setLayout (new BorderLayout ());
            treeTable = new MyTreeTable ();
            treeTable.setTreePreferredWidth (200);
            treeTable.setRootVisible (false);
            treeTable.setVerticalScrollBarPolicy 
                (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            treeTable.setHorizontalScrollBarPolicy 
                (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add (treeTable, "Center");  //NOI18N
    }
    
    public void setModel (CompoundModel model) {
        if (model == null) {
            getExplorerManager ().setRootContext (
                new AbstractNode (Children.LEAF)
            );
            return;
        }
        
        ColumnModel[] cs = model.getColumns ();
        Node.Property[] ps = new Node.Property [cs.length];
        int i, k = cs.length;
        for (i = 0; i < k; i++) {
            ps [i] = new Column (
                cs [i].getID (), 
                cs [i].getType (), 
                cs [i].getDisplayName (), 
                cs [i].getShortDescription (),
                cs [i].getPropertyEditor (),
                cs [i].initiallyVisible (),
                cs [i].isSortable (),
                cs [i].initiallySorted (),
                cs [i].initiallySortedDescending ()
            );
        }
        treeTable.setProperties (ps);
//        try {
//            treeTable.setToolTipText (model.getShortDescription (
//                model.getRoot ()
//            ));
//        } catch (ComputingException ex) {
//        } catch (UnknownTypeException ex) {
//            ex.printStackTrace ();
//        }
        getExplorerManager ().setRootContext (
            new TreeModelRoot (model).getRootNode ()
        );
    }
    
    public ExplorerManager getExplorerManager () {
        if (explorerManager == null) {
            explorerManager = new ExplorerManager ();
            explorerManager.addPropertyChangeListener (this);
        }
        return explorerManager;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (
            !evt.getPropertyName ().equals (ExplorerManager.PROP_SELECTED_NODES)
        ) return;
        
        TopComponent tc = (TopComponent) SwingUtilities.
            getAncestorOfClass (TopComponent.class, this);
        if (tc == null) return;
        tc.setActivatedNodes ((Node[]) evt.getNewValue ());
    }
    
    private static class MyTreeTable extends TreeTableView {
        MyTreeTable () {
            super ();
            treeTable.setShowHorizontalLines (true);
            treeTable.setShowVerticalLines (false);
        }
    }
    
    private static class Column extends PropertySupport.ReadWrite {

        private PropertyEditor propertyEditor;
        
        
        Column (
            String id,
            Class type,
            String displayName,
            String tooltip,
            PropertyEditor propertyEditor,
            boolean visible,
            boolean sortable,
            boolean sorted,
            boolean descending
        ) {
            super (
                id,
                type,
                displayName,
                tooltip
            );
            setValue ("InvisibleInTreeTableView", new Boolean (!visible));
            setValue ("ComparableColumnTTV", new Boolean (sortable));
            setValue ("SortingColumnTTV", new Boolean (sorted));
            setValue ("DescendingOrderTTV", new Boolean (descending));
            this.propertyEditor = propertyEditor;
        }
        
        public Object getValue () {
            return null;
        }
        
        public void setValue (Object obj) {
        }
        
        public PropertyEditor getPropertyEditor () {
            return propertyEditor;
        }
    }
}

