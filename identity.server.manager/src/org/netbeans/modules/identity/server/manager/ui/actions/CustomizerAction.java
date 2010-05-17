/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                NbBundle.getMessage(CustomizerAction.class, "LBL_ServerInstanceNode"),
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
