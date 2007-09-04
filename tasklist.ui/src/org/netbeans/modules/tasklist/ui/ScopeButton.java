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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.netbeans.spi.tasklist.TaskScanningScope;

/**
 *
 * @author S. Aubrecht
 */
class ScopeButton extends JToggleButton implements PropertyChangeListener {
    
    private TaskManagerImpl tm;
    private TaskScanningScope scope;
    
    /** Creates a new instance of ScopeButton */
    public ScopeButton( TaskManagerImpl tm, TaskScanningScope scope ) {
        this.tm = tm;
        this.scope = scope;
        setText( null );
        setIcon( new ImageIcon( Accessor.getIcon( scope ) ) );
        setToolTipText( Accessor.getDescription( scope ) );
        setSelected( scope.equals( tm.getScope() ) );
        setFocusable( false );
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        tm.addPropertyChangeListener( TaskManagerImpl.PROP_SCOPE, this );
        setSelected( scope.equals( tm.getScope() ) );
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        tm.removePropertyChangeListener( TaskManagerImpl.PROP_SCOPE, this );
    }
    
    @Override
    protected void fireActionPerformed( ActionEvent event ) {
//        if( isSelected() ) {
//            return;
//        }
        super.fireActionPerformed( event );
        switchScope();
    }
    
    private void switchScope() {
        if( scope.equals( tm.getScope() ) ) {
            setSelected( true );
            return;
        }
        tm.observe( scope, tm.getFilter() );
        setSelected( true );
        Settings.getDefault().setActiveScanningScope( scope );
    }

    public void propertyChange( PropertyChangeEvent e ) {
        setSelected( scope.equals( tm.getScope() ) );
    }
}
