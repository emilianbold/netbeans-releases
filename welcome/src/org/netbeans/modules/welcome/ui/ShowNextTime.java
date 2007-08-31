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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.WelcomeOptions;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;

/**
 *
 * @author S. Aubrecht
 */
class ShowNextTime extends JPanel 
        implements ActionListener, Constants, PropertyChangeListener {

    private JCheckBox button;

    /** Creates a new instance of RecentProjects */
    public ShowNextTime() {
        super( new BorderLayout() );

        setOpaque( false );
        
        button = new JCheckBox( BundleSupport.getLabel( "ShowOnStartup" ) ); // NOI18N
        button.setSelected( WelcomeOptions.getDefault().isShowOnStartup() );
        button.setOpaque( false );
        BundleSupport.setAccessibilityProperties( button, "ShowOnStartup" ); //NOI18N
        add( button, BorderLayout.CENTER );
        button.addActionListener( this );
    }
    
    public void actionPerformed(ActionEvent e) {
        WelcomeOptions.getDefault().setShowOnStartup( button.isSelected() );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        WelcomeOptions.getDefault().addPropertyChangeListener( this );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        WelcomeOptions.getDefault().removePropertyChangeListener( this );
    }

    public void propertyChange(PropertyChangeEvent evt) {
        button.setSelected( WelcomeOptions.getDefault().isShowOnStartup() );
    }
    
    
}
