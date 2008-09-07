/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.ui;

import java.awt.Dialog;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.impl.ServerNodeProvider;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author David
 */
public class PropertiesDialog  {
    private static final Logger LOGGER = Logger.getLogger(PropertiesDialog.class.getName());
    private static final String HELP_CTX = PropertiesDialog.class.getName();
    
    public enum Tab { BASIC, ADMIN };
    
    private final JTabbedPane tabbedPane;
    private final BasePropertiesPanel basePanel;
    private final AdminPropertiesPanel adminPanel;
    private final DatabaseServer server;

    public PropertiesDialog(DatabaseServer server) {
        this.server = server;
        
        basePanel = new BasePropertiesPanel(server);
        adminPanel = new AdminPropertiesPanel(server);
        
        tabbedPane = createTabbedPane(basePanel, adminPanel);
    }
    
    /**
     * Display the properties dialog
     * 
     * @return true if the user confirmed changes, false if they cancelled
     */
    public boolean displayDialog() {
        return displayDialog(Tab.BASIC);
    }

    /**
     * Display the properties dialog, choosing which tab you want to have
     * initial focus
     * 
     * @param tab
     * @return true if the user confirmed changes, false if they cancelled
     */
    public boolean displayDialog(Tab focusTab) {
        DialogDescriptor descriptor = createDialogDescriptor();
        if ( focusTab == Tab.ADMIN ) {
            tabbedPane.setSelectedIndex(1);
        }
        
        boolean ok = displayDialog(descriptor);
        
        if ( ok ) {
            updateServer();
        }     
        
        return ok;
    }
    
    private static JTabbedPane createTabbedPane(JPanel basePanel, JPanel adminPanel) {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        
        tabbedPane.addTab(
            getMessage("PropertiesDialog.BasePanelTitle"),
            /* icon */ null, basePanel,
            getMessage("PropertiesDialog.BasePanelHint"));
                
        tabbedPane.addTab(
            getMessage("PropertiesDialog.AdminPanelTitle"),
            /* icon */ null, adminPanel,
            getMessage("PropertiesDialog.AdminPanelHint"));
        
        tabbedPane.getAccessibleContext().setAccessibleName(
                getMessage("PropertiesDialog.ACS_Name"));
        tabbedPane.getAccessibleContext().setAccessibleDescription(
                getMessage("PropertiesDialog.ACS_Desc"));

        
        return tabbedPane;
    }
    
    private DialogDescriptor createDialogDescriptor() {
        DialogDescriptor descriptor = new DialogDescriptor(
                tabbedPane, 
                getMessage("PropertiesDialog.Title"));
        descriptor.setHelpCtx(new HelpCtx(HELP_CTX));
        
        basePanel.setDialogDescriptor(descriptor);
        adminPanel.setDialogDescriptor(descriptor);

        return descriptor;
    }

    private boolean displayDialog(DialogDescriptor descriptor) {
        assert SwingUtilities.isEventDispatchThread();
        

        for (;;) {                    
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
            dialog.dispose();

            if (!DialogDescriptor.OK_OPTION.equals(descriptor.getValue())) {
                return false;
            }

            return true;
        }
        
    }

    private void updateServer() {
        server.setHost(basePanel.getHost());
        server.setPort(basePanel.getPort());
        server.setUser(basePanel.getUser());
        server.setPassword(basePanel.getPassword());
        server.setSavePassword(basePanel.getSavePassword());
        
        server.setAdminPath(adminPanel.getAdminPath());
        server.setAdminArgs(adminPanel.getAdminArgs());
        server.setStartPath(adminPanel.getStartPath());
        server.setStartArgs(adminPanel.getStartArgs());
        server.setStopPath(adminPanel.getStopPath());
        server.setStopArgs(adminPanel.getStopArgs());

        ServerNodeProvider provider = ServerNodeProvider.getDefault();
        if ( ! provider.isRegistered() ) {
            // setRegistered will connect the server
            provider.setRegistered(true);
            
        } 
    }
        
    private static String getMessage(String id) {
        return NbBundle.getMessage(PropertiesDialog.class, id);
    }
}
