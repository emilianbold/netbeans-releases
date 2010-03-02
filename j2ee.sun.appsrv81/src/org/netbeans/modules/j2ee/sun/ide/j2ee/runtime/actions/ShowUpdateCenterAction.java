/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions;

import java.io.File;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.ManagerNode;
import org.openide.execution.NbProcessDescriptor;

/** 
 * This action will run the update center tool for Glassfish.
 * 
 * @author Peter Williams
 */
public class ShowUpdateCenterAction extends CookieAction {

    // TransactionService had a reasonable icon to use...
    private static final String SHOW_UPDATE_CENTER_ICONBASE = 
            "org/netbeans/modules/j2ee/sun/ide/resources/TransactionService.gif"; // NOI18N

    
    protected Class [] cookieClasses() {
        return new Class [] { /* SourceCookie.class */ };
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected void performAction(Node [] nodes) {
        if(nodes == null || nodes.length != 1) {
            return;
        }

        ManagerNode managerNode = nodes[0].getLookup().lookup(ManagerNode.class);
        if(managerNode != null) {
            try {
                // updatetool already running?  if yes - set focus, return
                // if no then....

                // locate updatecenter executable 
                File launcher = ServerLocationManager.getUpdateCenterLauncher(
                        managerNode.getDeploymentManager().getPlatformRoot());
                if(launcher != null) {
                    // run update tool and log process handle
                    new NbProcessDescriptor(launcher.getPath(), "").exec();
                }
            } catch (Exception ex){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(ShowUpdateCenterAction.class, "LBL_ShowUpdateCenterAction"); // NOI18N
    }
    
    @Override
    protected String iconResource() {
        return SHOW_UPDATE_CENTER_ICONBASE;
    }
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RefreshAction.class);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        return ViewLogAction.isOneLocalNodeChosen(nodes); // nodes != null && nodes.length == 1 && nodes[0] != null;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
