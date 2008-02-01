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
package org.netbeans.modules.sun.manager.jbi.actions;

import com.sun.esb.management.api.administration.AdministrationService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.esb.management.common.data.IEndpointStatisticsData;
import java.util.Date;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Action to show endpoint statistics of a SE/BC.
 * 
 * @author jqian
 */
public class ShowComponentEndpointsStatisticsAction extends NodeAction {
    
    protected void performAction(Node[] activatedNodes) {
       
        for (Node node : activatedNodes) {
            JBIComponentNode componentNode =
                    node.getLookup().lookup(JBIComponentNode.class);
            String compName = node.getName();
            
            InputOutput io = IOProvider.getDefault().getIO("Endpoint Statistics", false);
            io.select();
            OutputWriter writer = io.getOut();
            
            writer.println("==================================================");
            writer.println("Endpoint Statistics for " + compName + "  (" + new Date() + ")");
            writer.println();
            
            try {
                AppserverJBIMgmtController controller = 
                        componentNode.getAppserverJBIMgmtController();
                AdministrationService adminService = 
                        controller.getAdministrationService();
                PerformanceMeasurementServiceWrapper perfService = 
                        controller.getPerformanceMeasurementServiceWrapper();
                
                writer.println(" * Provisioning Endpoints:");
                String[] pEndpoints = 
                        adminService.getProvisioningEndpoints(compName, 
                        AppserverJBIMgmtController.SERVER_TARGET);
                for (String pEndpoint : pEndpoints) {
                    if (pEndpoint.endsWith(",Provider")) {
                        pEndpoint = pEndpoint.substring(0, pEndpoint.length() - 9);
                    }
                    IEndpointStatisticsData statistics = 
                            perfService.getEndpointStatistics(pEndpoint, 
                            AppserverJBIMgmtController.SERVER_TARGET);
                    writer.println("   " + pEndpoint);
                    writer.println(statistics.getDisplayString());                   
                }
                
                writer.println(" * Consuming Endpoints:");
                String[] cEndpoints = 
                        adminService.getConsumingEndpoints(compName, 
                        AppserverJBIMgmtController.SERVER_TARGET);
                for (String cEndpoint : cEndpoints) {
                    if (cEndpoint.endsWith(",Consumer")) {
                        cEndpoint = cEndpoint.substring(0, cEndpoint.length() - 9);
                    }
                    IEndpointStatisticsData statistics = 
                            perfService.getEndpointStatistics(cEndpoint, 
                            AppserverJBIMgmtController.SERVER_TARGET);
                    writer.println("   " + cEndpoint);
                    writer.println(statistics.getDisplayString());
                }
                writer.println();
            } catch (ManagementRemoteException e) {
                System.err.println(e.getMessage());
            }
        }
    }
        
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
         
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return "Show Endpoint Statistics"; //NbBundle.getMessage(RefreshAction.class, "LBL_RefreshAction"); // NOI18N
    }    
}
