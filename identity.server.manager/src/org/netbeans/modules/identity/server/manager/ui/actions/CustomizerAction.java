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

package org.netbeans.modules.identity.server.manager.ui.actions;

import java.awt.Dialog;
import org.netbeans.modules.identity.server.manager.api.ServerInstance;
import org.netbeans.modules.identity.server.manager.ui.EditDialogDescriptor;
import org.netbeans.modules.identity.server.manager.ui.ServerConfigEditorPanel;
import org.netbeans.modules.identity.server.manager.ui.ServerInstanceNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action for bring up the Properties editor for the AM service instance.
 *
 * Created on June 14, 2006, 7:01 PM
 *
 * @author ptliu
 */
public class CustomizerAction extends NodeAction {
    
    private static final String HELP_ID = "idmtools_am_changing_props_am_instance"; //NOI18N
    
    /** Creates a new instance of CustomizerAction */
    public CustomizerAction() {
    }
    
    protected void performAction(Node[] activatedNodes) {
        ServerInstanceNode node = (ServerInstanceNode) activatedNodes[0].getLookup().lookup(ServerInstanceNode.class);
        ServerInstance instance = node.getInstance();
        final ServerConfigEditorPanel panel = new ServerConfigEditorPanel(instance);
        EditDialogDescriptor descriptor = new EditDialogDescriptor(
                panel, 
                instance.getDisplayName(),
                false,
                panel.getEditableComponents(),
                getHelpCtx()) {
            public String validate() {
                return panel.checkValues();
            }
        };
        
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
            
            if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                panel.updateInstance();
            }
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(CustomizerAction.class,
                "LBL_Customizer");
    }
 
    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
}
