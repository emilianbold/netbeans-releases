/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.ActionButton;
import org.netbeans.modules.welcome.content.BackgroundPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author S. Aubrecht
 */
class PluginsPanel extends BackgroundPanel {

    public PluginsPanel() {
        super( new BorderLayout() );
        JPanel center = new BackgroundPanel( new GridBagLayout() );
        JLabel lbl = new JLabel("<html>" + BundleSupport.getLabel( "PluginsContent" ) ); //NOI18N
        lbl.setBorder( BorderFactory.createEmptyBorder(0, 0, 0, 0) );
        
        center.add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,new Insets(0,0,15,0),0,0) );
        center.add( new JLabel(), new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );

        add( center, BorderLayout.CENTER );
        JPanel bottom = new BackgroundPanel( new GridBagLayout() );
        
        bottom.add( new ActionButton( new ShowPluginManagerAction(), false, null ), 
                new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
        
        bottom.add( new JLabel(), new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );
        
//        bottom.add( new JLabel(), new GridBagConstraints(2,0,1,1,1.0,0.0,GridBagConstraints.EAST,
//                GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );
        add( bottom, BorderLayout.SOUTH );
    }

    private static class ShowPluginManagerAction extends AbstractAction {
        public ShowPluginManagerAction() {
            super( BundleSupport.getLabel( "AddPlugins" ) );
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
