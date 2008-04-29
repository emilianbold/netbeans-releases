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

package org.netbeans.modules.glassfish.common.actions;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;

/** 
 * This action will run the update center tool for Glassfish.
 * 
 * @author Peter Williams
 */
public class ViewUpdateCenterAction extends NodeAction {

    private static final String SHOW_UPDATE_CENTER_ICONBASE = 
            "org/netbeans/modules/glassfish/common/resources/UpdateCenter.gif"; // NOI18N
    
    @Override
    protected void performAction(Node[] nodes) {
        GlassfishModule commonSupport = nodes[0].getLookup().lookup(GlassfishModule.class);
        if(commonSupport != null) {
            try {
                // updatetool already running?  if yes - set focus, return
                // if no then....

                // locate updatecenter executable 
                String installRoot = commonSupport.getInstanceProperties().get(GlassfishModule.HOME_FOLDER_ATTR);
                File launcher = getUpdateCenterLauncher(new File(installRoot));
                
                // !PW FIXME check to see if run before, if not, run once to download.
                
                if(launcher != null) {
                    // run update tool and log process handle
                    new NbProcessDescriptor(launcher.getPath(), "").exec();
                } else {
                    String message = NbBundle.getMessage(ViewUpdateCenterAction.class, 
                            "MSG_UpdateCenterNotFound", installRoot);
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            } catch (Exception ex){
                Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Locate update center launcher within the glassfish installation
     *   [installRoot]/updatecenter/bin/updatetool[.BAT]
     * 
     * @param asInstallRoot appserver install location
     * @return File reference to launcher, or null if not found.
     */
    private File getUpdateCenterLauncher(File asInstallRoot) {
        File result = null;
        if(asInstallRoot != null && asInstallRoot.exists()) {
            File updateCenterBin = new File(asInstallRoot, "bin"); // NOI18N
            if(updateCenterBin.exists()) {
                String launcher = "updatetool"; // NOI18N
                if(Utilities.isWindows()) {
                    launcher += ".BAT"; // NOI18N
                }
                File launcherPath = new File(updateCenterBin, launcher);
                result = (launcherPath.exists()) ? launcherPath : null;
            }
        }
        return result;
    }    
    
    @Override
    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 && nodes[0] != null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewUpdateCenterAction.class, "CTL_ViewUpdateCenterAction");
    }

    @Override
    protected String iconResource() {
        return SHOW_UPDATE_CENTER_ICONBASE;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
