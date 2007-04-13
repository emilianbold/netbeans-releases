/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.tasklist.impl.*;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
class Util {
    
    /** Creates a new instance of Util */
    private Util() {
    }
    
    /**
     * 
     * @param t 
     * @return 
     */
    public static Action getDefaultAction( Task t ) {
        return new OpenTaskAction( t );
    }
    
    public static JPopupMenu createPopup( TaskListTable table ) {
        JPopupMenu popup = new JPopupMenu();
        //TODO fix
        //show source
        Task t = table.getSelectedTask();
        if( null != t ) {
            popup.add( getDefaultAction(t) );
            popup.addSeparator();
        }
        //scope
        JMenu scopeMenu = new JMenu( NbBundle.getMessage( Util.class, "LBL_Scope" ) ); //NOI18N
        ScanningScopeList scopeList = ScanningScopeList.getDefault();
        for( TaskScanningScope scope : scopeList.getTaskScanningScopes() ) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem( new SwitchScopeAction(scope) );
            item.setSelected( scope.equals( TaskManagerImpl.getInstance().getScope() ) );
            scopeMenu.add( item );
        }
        popup.add( scopeMenu );
        //filter
        JMenu filterMenu = new JMenu( NbBundle.getMessage( Util.class, "LBL_Filter" ) ); //NOI18N
        FiltersMenuButton.fillMenu( null, filterMenu );
        popup.add( filterMenu );
        
        popup.addSeparator();
        //refresh
        popup.add( new RefreshAction() );
        popup.addSeparator();
        //list options
        JMenu sortMenu = createSortMenu( table );
        popup.add( sortMenu );
        
        return popup;
    }
    
    private static JMenu createSortMenu( TaskListTable table ) {
        JMenu res = new JMenu( NbBundle.getMessage( Util.class, "LBL_SortBy" ) ); //NOI18N
        for( int i=1; i<table.getColumnCount(); i++ ) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem( new SwitchSortAction( table, i ) );
            item.setSelected( i == table.getSortColumn() );
            res.add( item );
        }
        res.addSeparator();
        JRadioButtonMenuItem item = new JRadioButtonMenuItem( new SwitchSortOrderAction(table, true) );
        item.setSelected( table.isAscendingSort() );
        res.add( item );
        item = new JRadioButtonMenuItem( new SwitchSortOrderAction(table, false) );
        item.setSelected( !table.isAscendingSort() );
        res.add( item );
        return res;
    }
    
    private static class SwitchScopeAction extends AbstractAction {
        private TaskScanningScope scope;
        public SwitchScopeAction( TaskScanningScope scope ) {
            super( Accessor.getDisplayName( scope ), new ImageIcon( Accessor.getIcon( scope ) ) );
            this.scope = scope;
        }
    
        public void actionPerformed( ActionEvent e ) {
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( scope, tm.getFilter() );
        }
    }
    
    private static class RefreshAction extends AbstractAction {
        public RefreshAction() {
            super( NbBundle.getMessage( Util.class, "LBL_Refresh" ) ); //NOI18N
        }
    
        public void actionPerformed( ActionEvent e ) {
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.refresh( tm.getScope() );
        }
    }
    
    private static class SwitchSortAction extends AbstractAction {
        private TaskListTable table;
        private int col;
        
        public SwitchSortAction( TaskListTable table, int col ) {
            super( table.getModel().getColumnName(col) );
            this.table = table;
            this.col = col;
        }
    
        public void actionPerformed( ActionEvent e ) {
            if( col == table.getSortColumn() )
                table.setSortColumn( -1 );
            else
                table.setSortColumn( col );
            table.getTableHeader().repaint();
        }
    }
    
    private static class SwitchSortOrderAction extends AbstractAction {
        private TaskListTable table;
        private boolean asc;
        
        public SwitchSortOrderAction( TaskListTable table, boolean asc ) {
            super( asc
                ? NbBundle.getMessage( Util.class, "LBL_Asc" ) //NOI18N
                : NbBundle.getMessage( Util.class, "LBL_Desc" ) ); //NOI18N
            this.table = table;
            this.asc = asc;
        }
    
        public void actionPerformed( ActionEvent e ) {
            table.setAscendingSort( asc );
            table.getTableHeader().repaint();
        }
    }
}
