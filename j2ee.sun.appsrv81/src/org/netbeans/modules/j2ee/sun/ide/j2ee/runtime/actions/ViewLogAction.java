/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions;

import java.io.File;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.*;
import org.netbeans.modules.j2ee.sun.ide.j2ee.LogViewerSupport;

import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
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
            viewLog(sdm);
        }
        
        
        
    }
    public static void viewLog(SunDeploymentManagerInterface sdm){
            try{
                if(sdm.isLocal()==false){
                    return;
                }

                DeploymentManagerProperties dmProps = new DeploymentManagerProperties((DeploymentManager) sdm);
                String domainRoot = dmProps.getLocation();
                if (domainRoot == null) {
                    return;
                }
                String domain = dmProps.getDomainName();
                // XXX the dm props has the domain directory....
                //File f = new File(installRoot+"/domains/"+domain+"/logs/server.log");
                File f = new File(domainRoot+File.separator+domain+"/logs/server.log");
                LogViewerSupport p = new LogViewerSupport( f ,sdm.getHost()+":"+sdm.getPort());
                p.showLogViewer();
            }
            catch (Exception e){
                Util.showInformation(e.getLocalizedMessage());
            }    
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
         if( (nodes == null) || (nodes.length < 1) )
             return false;

        if(nodes[0].getLookup().lookup(ManagerNode.class) != null){
             try{
                 ManagerNode node = (ManagerNode)nodes[0].getLookup().lookup(ManagerNode.class);
                 
                 
                 SunDeploymentManagerInterface sdm = node.getDeploymentManager();
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
