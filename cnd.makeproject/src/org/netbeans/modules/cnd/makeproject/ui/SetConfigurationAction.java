/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

public class SetConfigurationAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {
    
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_KEY = "org.netbeans.modules.cnd.makeproject.ui.ConfigurationItem"; // NOI18N
    
    private JMenu subMenu;

    private Project project;
    
    /** Creates a new instance of BrowserAction */
    public SetConfigurationAction(Project project) {
        super( NbBundle.getMessage( SetConfigurationAction.class, "LBL_SetConfigurationAction_Name"),   // NOI18N
               null );
	this.project = project;
        //OpenProjectList.getDefault().addPropertyChangeListener( this );
    }
    
        
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }

    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return subMenu;
    }
    
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }
        
    private void createSubMenu() {
        if ( subMenu == null ) {
            String label = NbBundle.getMessage( SetConfigurationAction.class, "LBL_SetConfigurationAction_Name" ); // NOI18N
            subMenu = new JMenu( label );
        }
        
        subMenu.removeAll();
        ActionListener jmiActionListener = new MenuItemActionListener(); 
        
	ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
	if (pdp == null)
	    return;

	ConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
	Configuration[] confs = projectDescriptor.getConfs().getConfs();
        // Fill menu with items
        for ( int i = 0; i < confs.length; i++ ) {
            JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(confs[i].getName(), confs[i].isDefault());
            subMenu.add(jmi);
            jmi.putClientProperty(PROJECT_KEY, projectDescriptor);
            jmi.addActionListener(jmiActionListener);
        }
	// Now add the Configurations.. action. Do it in it's own menu item othervise it will get shifted to the right.
	subMenu.add(new JSeparator());
	JMenuItem profilesMenuItem = new JMenuItem(NbBundle.getMessage(SetConfigurationAction.class, "LBL_ConfigurationsAction_Name")); // NOI18N
	subMenu.add(profilesMenuItem);
	profilesMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed (ActionEvent event) {
		CommonProjectActions.customizeProjectAction().actionPerformed(new ActionEvent(this, -1, null));
	    }
	});

        subMenu.setEnabled(confs.length > 0 );
    }
    
    // Innerclasses ------------------------------------------------------------
    private static class MenuItemActionListener implements ActionListener {
        public void actionPerformed( ActionEvent e ) {
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
		ConfigurationDescriptor projectDescriptor = (ConfigurationDescriptor)jmi.getClientProperty( PROJECT_KEY );
                if (projectDescriptor != null ) {
                    projectDescriptor.getConfs().setActive(jmi.getText());
		    projectDescriptor.setModified();
                }
                
            }
            
        }
        
    }
    
}
