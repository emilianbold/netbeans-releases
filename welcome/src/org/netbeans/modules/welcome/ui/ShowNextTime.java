/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.welcome.WelcomeOptions;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;

/**
 *
 * @author S. Aubrecht
 */
public class ShowNextTime extends JPanel implements ActionListener, Constants {

    private JCheckBox button;

    /** Creates a new instance of RecentProjects */
    public ShowNextTime() {
        super( new BorderLayout() );

        setOpaque( false );
        
        button = new JCheckBox( BundleSupport.getLabel( "ShowOnStartup" ) ); // NOI18N
        button.setSelected( WelcomeOptions.getDefault().isShowOnStartup() );
        button.setFont( BUTTON_FONT );
        button.setForeground( BUTTON_TEXT_COLOR );
        button.setBackground( DEFAULT_BACKGROUND_COLOR );
        button.setOpaque( false );
        add( button, BorderLayout.CENTER );
        button.addActionListener( this );
    }
    
    public void stateChanged(ChangeEvent e) {
        WelcomeOptions.getDefault().setShowOnStartup( button.isSelected() );
    }

    public void actionPerformed(ActionEvent e) {
        WelcomeOptions.getDefault().setShowOnStartup( button.isSelected() );
    }
}
