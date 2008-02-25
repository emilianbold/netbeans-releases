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

package org.netbeans.modules.websvc.saas.services.strikeiron.actions;

import java.awt.Dialog;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.services.strikeiron.ui.FindServiceUI;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceData;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Search for web services offered through StrikeIron marketplace.
 *
 * @author nam
 */
public class FindServiceAction extends NodeAction {

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(FindServiceAction.class, "LBL_FindStrikeIronServices");
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected void performAction(Node[] activatedNodes) {
        JButton addButton = new JButton();
        JButton closeButton = new JButton();
        FindServiceUI panel = new FindServiceUI(addButton, closeButton);
        closeButton.setDefaultCapable(false);
        DialogDescriptor desc = new DialogDescriptor(
                                    panel,
                                    NbBundle.getMessage (FindServiceUI.class, "LBL_FindStrikeIronServices"),
                                    true, 
                                    new JButton[]{ panel.getAddButton(), panel.getCancelButton() },
                                    null,
                                    DialogDescriptor.DEFAULT_ALIGN,
                                    null,
                                    null /*final ActionListener bl*/);
        desc.setOptions(new Object[0]);
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (desc);
        dialog.setVisible(true);
        if (desc.getValue() == panel.getAddButton()) {
            SaasGroup group = SaasServicesModel.getInstance().getRootGroup().getChildGroup(
                    StrikeIronActionsProvider.STRIKE_IRON_GROUP);
            if (group == null) {
                Logger.global.log(Level.INFO, "Could not find 'StrikeIron Services' group"); //NOI18N
                return;
            }
            Set<? extends WsdlServiceData> services = panel.getSelectedServices();
            for (WsdlServiceData service : services) {
                String url = service.getUrl();
                String name = service.getServiceName();
                if (WsdlUtil.findWsdlData(url, name) == null) {
                    WsdlSaas saas = SaasServicesModel.getInstance().createWsdlService(group, name, url, service.getPackageName());
                    saas.getDelegate().setApiDoc(service.getInfoPage());
                }
            }
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}