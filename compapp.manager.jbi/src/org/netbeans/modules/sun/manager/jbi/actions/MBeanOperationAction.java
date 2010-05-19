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
package org.netbeans.modules.sun.manager.jbi.actions;

import com.sun.esb.management.api.configuration.ConfigurationService;
import com.sun.esb.management.common.ManagementRemoteException;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.netbeans.modules.sun.manager.jbi.nodes.Refreshable;
import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * An action for an MBean operation (w/o any parameters) that is 
 * not known until runtime.
 * 
 * @author jqian
 */
// Can not use NetBeans' SystemAction because of sharability issue.
public class MBeanOperationAction extends AbstractAction {

    private String mBeanKey;
    private String operationName;
    private String description;

    public MBeanOperationAction(
            String mBeanKey, String operationName,
            String displayName, String description, boolean enabled) {
        super(displayName);

        this.mBeanKey = mBeanKey;
        this.operationName = operationName;
        this.description = description;

        setEnabled(enabled);
    }

    public void actionPerformed(ActionEvent ev) {

        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        JBIComponentNode componentNode =
                activatedNodes[0].getLookup().lookup(JBIComponentNode.class);

        final String componentName = componentNode.getName();

        final AppserverJBIMgmtController controller =
                componentNode.getAppserverJBIMgmtController();

        String title =
                NbBundle.getMessage(MBeanOperationAction.class,
                "LBL_Invoking_MBean_Operation", // NOI18N
                new Object[]{operationName});
        final ProgressUI progressUI = new ProgressUI(title, false);
        progressUI.start();

        // Invoke the action out of EDT 'cause the operation could be expensive.
        new Thread() {

            @Override
            public void run() {
                try {
                    ConfigurationService configService = controller.getConfigurationService();
                    final String result = (String) configService.invokeExtensionMBeanOperation(
                            componentName, mBeanKey, operationName,
                            new Object[]{}, new String[]{},
                            AppserverJBIMgmtController.SERVER_TARGET, null);

                    Lookup lookup = activatedNodes[0].getLookup();
                    final Refreshable refreshable =
                            lookup.lookup(Refreshable.class);
                    if (refreshable != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            
                            public void run() {
                                if (result != null) {
                                    NotifyDescriptor d = new NotifyDescriptor.Message(
                                            result,
                                            NotifyDescriptor.INFORMATION_MESSAGE);
                                    DialogDisplayer.getDefault().notify(d);
                                }
                                refreshable.refresh();
                            }
                        });
                    }
                } catch (ManagementRemoteException e) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            e.getMessage(),
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        
                        public void run() {
                            progressUI.finish();
                        }
                    });                    
                }
            }
        }.start();
    }
}
