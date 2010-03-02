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
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.netbeans.modules.j2ee.sun.ide.j2ee.LogViewerSupport;

import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.ManagerNode;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
import org.openide.windows.InputOutput;
/** Action to get the log viewer dialog to open
 *
 * @author ludo
 */
public class ViewLogAction extends CookieAction {
    
    protected Class[] cookieClasses() {
        return new Class[] {/* SourceCookie.class */};
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
        // return MODE_ALL;
    }
    
    protected void performAction(Node[] nodes) {
        if(nodes[0].getLookup().lookup(ManagerNode.class) != null){
            ManagerNode node = (ManagerNode)nodes[0].getCookie(ManagerNode.class);
            SunDeploymentManagerInterface sdm = node.getDeploymentManager();
            viewLog(sdm,true,true);//entire file and forced
        }
        
        
        
    }
    public static InputOutput viewLog(SunDeploymentManagerInterface sdm){
        return viewLog(sdm,false,false);//not the entire file and no forced refresh, just a front view    
    }
    
    private static InputOutput viewLog(SunDeploymentManagerInterface sdm, boolean entireFile, boolean forced){
        try{
            if(sdm.isLocal()==false){
                return null;
            }
            
            DeploymentManagerProperties dmProps = new DeploymentManagerProperties((DeploymentManager) sdm);
            String domainRoot = dmProps.getLocation();
            if (domainRoot == null) {
                return null;
            }
            String domain = dmProps.getDomainName();
            // FIXME -- the log file can be renamed by the user in the admin gui
            //  we probably need to get the property value and use it... not this 
            //  hard coded value....
            File f = new File(domainRoot+File.separator+domain+"/logs/server.log");
            LogViewerSupport p = LogViewerSupport.getLogViewerSupport(f , dmProps.getUrl(),2000,entireFile);
            return p.showLogViewer(forced);
        } catch (Exception e){
            Util.showInformation(e.getLocalizedMessage());
        }
        return null;
    }
    public String getName() {
        return NbBundle.getMessage(ViewLogAction.class, "LBL_ViewlogAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/sun/ide/resources/AddInstanceActionIcon.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RefreshAction.class);
    }
    
    protected boolean enable(Node[] nodes) {
        return isOneLocalNodeChosen(nodes);
    }

    static boolean isOneLocalNodeChosen(Node[] nodes) {
        if( (nodes == null) || (nodes.length < 1) ) {
            return false;
        }
        if (nodes.length > 1) {
            return false;
        }
        if(nodes[0].getLookup().lookup(ManagerNode.class) != null){
            try{
                ManagerNode node = (ManagerNode)nodes[0].getLookup().lookup(ManagerNode.class);
                
                
                SunDeploymentManagerInterface sdm = node.getDeploymentManager(false);
                return sdm.isLocal();
            } catch (Exception e){
                //nothing to do, the NetBeasn node system is wierd sometimes...
            }
        }
        return false;
    }
    
    /** Perform special enablement check in addition to the normal one.
     * protected boolean enable(Node[] nodes) {
     * if (!super.enable(nodes)) return false;
     * if (...) ...;
     * }
     */
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RefreshAction.class, "HINT_Action"));
     * }
     */
    
    protected boolean asynchronous() {
        return false;
    }
    
}
