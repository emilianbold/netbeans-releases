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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.ActionButton;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author S. Aubrecht
 */
class PluginsPanel extends JPanel {

    public PluginsPanel() {
        super( new BorderLayout() );
        setOpaque( false );
        add( new JLabel("<html>" + BundleSupport.getLabel( "PluginsContent" ) ), //NOI18N
                BorderLayout.CENTER );
        JPanel bottom = new JPanel( new GridBagLayout() );
        bottom.setOpaque( false );
        
        bottom.add( new JButton( new ShowPluginManagerAction() ), 
                new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
        
        bottom.add( new JLabel(), new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );
        
//        bottom.add( new JLabel(), new GridBagConstraints(2,0,1,1,1.0,0.0,GridBagConstraints.EAST,
//                GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );
        add( bottom, BorderLayout.SOUTH );
    }

    private static class ShowPluginManagerAction extends AbstractAction {
        public ShowPluginManagerAction() {
            super( "Add Plugins" );
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                ClassLoader cl = Lookup.getDefault ().lookup (ClassLoader.class);
                Class<CallableSystemAction> clz = (Class<CallableSystemAction>)cl.loadClass("org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction");
                CallableSystemAction a = CallableSystemAction.findObject(clz, true);
                a.performAction ();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
}
