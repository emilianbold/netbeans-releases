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
/*
 * ManagerNode.java
 *
 * Created on December 21, 2003, 8:29 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import java.util.Collection;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ShowAdminToolAction;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ShowUpdateCenterAction;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ViewLogAction;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Customizer;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;



/**
 *
 * @author  ludo
 */
public class ManagerNode extends AbstractNode implements Node.Cookie{
    static java.util.Collection bogusNodes = java.util.Arrays.asList(new Node[] { Node.EMPTY, Node.EMPTY });
    private SunDeploymentManagerInterface sdm;
    private DeploymentManager manager;
    public static final String DIR_ACTION_EXTENSION = "/J2EE/SunAppServer/Actions"; //NOI18N
    
    public ManagerNode(DeploymentManager manager) {
        super(new MyChildren(bogusNodes));
        sdm = (SunDeploymentManagerInterface)manager;
        this.manager = manager;
        setDisplayName(sdm.getHost()+":"+sdm.getPort());
        
        setIconBaseWithExtension("org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.png");//NOI18N
        setShortDescription(NbBundle.getMessage(ManagerNode.class, "HINT_node", sdm.getHost()+":"+sdm.getPort()));   //NOI18N
        getCookieSet().add(this);
        getCookieSet().add(sdm);
    }
    
    public Node.Cookie getCookie (Class type) {
        if (ManagerNode.class.isAssignableFrom(type)) {
            return this;
        }
        if (SunDeploymentManagerInterface.class.isAssignableFrom(type)) {
            return this;
        }
        return super.getCookie (type);
    }
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public java.awt.Component getCustomizer() {
        return new Customizer(manager);
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.getDefaultFileSystem().findResource(DIR_ACTION_EXTENSION);
        int nbextraoptions=0;
        FileObject[] ch =null;
        if(dir!=null){
            ch = dir.getChildren();
            nbextraoptions = ch.length; 
        }
        javax.swing.Action[]  newActions = new javax.swing.Action[5 + nbextraoptions] ;// 5 hardcoded number of actionns!!
        int a=0;
        newActions[a++]=(null);        
        newActions[a++]= (SystemAction.get(ShowAdminToolAction.class));
        newActions[a++]=(SystemAction.get(ViewLogAction.class));
        if(ServerLocationManager.hasUpdateCenter(sdm.getPlatformRoot())) {
            newActions[a++]=(SystemAction.get(ShowUpdateCenterAction.class));
        }
        boolean isGlassFish = ServerLocationManager.isGlassFish(sdm.getPlatformRoot());
        for(int i = 0; i < nbextraoptions; i++) {
            try{
                DataObject dobj = DataObject.find(ch[i]);
                InstanceCookie cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                    newActions[a+i]=null;

                if(cookie != null){
                    Class theActionClass = cookie.instanceClass();
                    String attr = (String) ch[i].getAttribute("8.x");//NOI18N
                    if (attr==null ){ //not extra attr defined: add the action
                        newActions[a+i]=(SystemAction.get(theActionClass));
                        
                    } else if (!isGlassFish){// add the action only if we are 8.x
                        newActions[a+i]=(SystemAction.get(theActionClass));
                        
                    }

                }
                
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        
        return newActions;
    }

    
    public String  getAdminURL() {
        if(sdm.isSecure()){
            return "https://"+sdm.getHost()+":"+sdm.getPort();//NOI18N
        } else{
             return "http://"+sdm.getHost()+":"+sdm.getPort();//NOI18N
           
        }
    }
    public SunDeploymentManagerInterface getDeploymentManager(){
        return getDeploymentManager(true);
    }
   
    public SunDeploymentManagerInterface getDeploymentManager(boolean needsRefresh){
        if (needsRefresh) {
            sdm.refreshDeploymentManager();
        }
        return sdm;
    }
    public HelpCtx getHelpCtx() {
        return null; //new HelpCtx ("AS_RTT_AppServer");//NOI18N
    }

    
    public static class MyChildren extends Children.Array {
        public MyChildren(Collection nodes) {
            super(nodes);
        }
    }
    

}
