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
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.tasklist.filter.FilterEditor;
import org.netbeans.modules.tasklist.filter.FilterRepository;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
public class FiltersMenuButton extends MenuToggleButton implements PropertyChangeListener {
    
    private TaskManagerImpl taskManager;
    
    /** Creates a new instance of FiltersMenuButton */
    public FiltersMenuButton( TaskFilter currentFilter ) {
        super( new ImageIcon( Utilities.loadImage( "org/netbeans/modules/tasklist/ui/resources/filter.png" ) ),  //NOI18N
                new ImageIcon( Utilities.loadImage( "org/netbeans/modules/tasklist/ui/resources/filter_rollover.png" ) ), 4 );  //NOI18N
        taskManager = TaskManagerImpl.getInstance();
        
        updateState( currentFilter );

        addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if( !isSelected() ) {
                    taskManager.observe( taskManager.getScope(), TaskFilter.EMPTY );
                } else {
                    openFilterEditor();
                    updateState( taskManager.getFilter() );
                }
            }
        });
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        taskManager.addPropertyChangeListener( TaskManagerImpl.PROP_FILTER, this );
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        taskManager.removePropertyChangeListener( TaskManagerImpl.PROP_FILTER, this );
    }
    
    @Override
    protected JPopupMenu getPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        fillMenu( popup, null );
        
        return popup;
    }
    
    static void fillMenu( JPopupMenu popup, JMenu menu ) {
        assert null != popup || null != menu;
        FilterRepository filterRep = FilterRepository.getDefault();
        TaskManagerImpl tm = TaskManagerImpl.getInstance();
        TaskFilter activeFilter = tm.getFilter();
        
        JRadioButtonMenuItem item = new JRadioButtonMenuItem( new CancelFilterAction() );
        item.setSelected( TaskFilter.EMPTY.equals( activeFilter ) );
        if( null == popup )
            menu.add( item );
        else
            popup.add( item );
        
        if( null == popup )
            menu.addSeparator();
        else
            popup.addSeparator();
        
        List<TaskFilter> allFilters = filterRep.getAllFilters();
        for( TaskFilter tf : allFilters ) {
            item = new JRadioButtonMenuItem( new SetFilterAction( tf ) );
            item.setSelected( activeFilter.equals( tf ) );
            if( null == popup )
                menu.add( item );
            else
                popup.add( item );
        }
        if( allFilters.size() > 0 ) {
            if( null == popup )
                menu.addSeparator();
            else
                popup.addSeparator();
        }
        
        if( null == popup )
            menu.add( new ManageFiltersAction() );
        else
            popup.add( new ManageFiltersAction() );
    }
    
    private static class CancelFilterAction extends AbstractAction {
        
        public CancelFilterAction() {
            super( NbBundle.getMessage( FiltersMenuButton.class, "LBL_CancelFilter" ) ); //NOI18N
        }
    
        public void actionPerformed(ActionEvent e) {
            FilterRepository.getDefault().setActive( null );
            try {
                FilterRepository.getDefault().save();
            } catch( IOException ioE ) {
                Logger.getLogger( FiltersMenuButton.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE );
            }
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( tm.getScope(), TaskFilter.EMPTY );
        }
    }
    
    private static class SetFilterAction extends AbstractAction {
        private TaskFilter filter;
        public SetFilterAction( TaskFilter filter ) {
            super( filter.getName() );
            this.filter = filter;
        }
    
        public void actionPerformed(ActionEvent e) {
            FilterRepository.getDefault().setActive( filter );
            try {
                FilterRepository.getDefault().save();
            } catch( IOException ioE ) {
                Logger.getLogger( FiltersMenuButton.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE );
            }
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( tm.getScope(), filter );
        }
    }
    
    private static class ManageFiltersAction extends AbstractAction {
        public ManageFiltersAction() {
            super( NbBundle.getMessage( FiltersMenuButton.class, "LBL_EditFilters" ) ); //NOI18N
        }
    
        public void actionPerformed(ActionEvent arg0) {
            openFilterEditor();
        }
    }

    public void propertyChange( PropertyChangeEvent e ) {
        updateState( taskManager.getFilter() );
    }
    
    private void updateState( TaskFilter filter ) {
        if( TaskFilter.EMPTY.equals( filter ) ) {
            setSelected( false );
            setToolTipText( NbBundle.getMessage( FiltersMenuButton.class, "HINT_SelectFilter" ) ); //NOI18N
            FilterRepository.getDefault().setActive( null );
        } else {
            setSelected( true );
            setToolTipText( filter.getName() );
            FilterRepository.getDefault().setActive( filter );
        }
    }

    
    private static void openFilterEditor() {
        FilterRepository filterRep = FilterRepository.getDefault();
        FilterRepository clone = (FilterRepository)filterRep.clone();
        FilterEditor fe = new FilterEditor( clone );
        if( fe.showWindow() ) {
            filterRep.assign(clone);
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( tm.getScope(), filterRep.getActive() );
            try {
                filterRep.save();
            } catch( IOException ioE ) {
                Logger.getLogger( FiltersMenuButton.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE );
            }
        }
    }
}
