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

package org.netbeans.modules.websvc.registry.netbeans;


import java.io.File;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import java.awt.Image;
import javax.swing.Action;
/**
 * The top level node representing Web Services in the Server Navigator
 * @author Ludovic
 */
public class WebServicesRootNodeNetBeansSide extends AbstractNode implements WebServicesRootNodeInterface/*, java.beans.PropertyChangeListener*/ {
    private static  WebServicesRootNodeInterface realNode;
    public WebServicesRootNodeNetBeansSide() {
        super(createChildrenNodes());
//        WebServiceModuleInstaller.findObject(WebServiceModuleInstaller.class).addPropertyChangeListener(this);
        setName("default-");
        
        if(realNode != null) {
            setDisplayName(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services"));
            setShortDescription(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services"));
        } else {
            setDisplayName(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services_Disabled"));
            setShortDescription(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services_Disabled_Desc"));
        }
    }
    
    public Image getIcon(int type){
        if (realNode!=null)
            return ImageUtilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup.png");
        else 
            return ImageUtilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup_invalid.png");
    }
    
    public Image getOpenedIcon(int type){
        if (realNode!=null)
            return ImageUtilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup.png");
        else
            return ImageUtilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup_invalid.png");
    }
    
    public Action[] getActions(boolean context) {

        if(realNode != null) {
            return realNode.getActions(context); 
        }
        return new Action[0];
    }
    
    public Action getPreferredAction() {
        if(realNode != null) {
            return realNode.getPreferredAction();
        }
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(WebServicesRootNodeInterface.class);
    }
    
    
    public Node.Cookie getCookie (Class type) {
        if(realNode != null) {
            return realNode.getCookie(type);
        }

        return null;
    }
    
    static public Children createChildrenNodes(){
        
        try{
            realNode = (WebServicesRootNodeInterface) WebServiceModuleInstaller.getExtensionClassLoader().loadClass("org.netbeans.modules.websvc.registry.nodes.WebServicesRootNode").newInstance();//NOI18N
            
            return  (Children.Keys) WebServiceModuleInstaller.getExtensionClassLoader().loadClass("org.netbeans.modules.websvc.registry.nodes.WebServicesRootNodeChildren").newInstance();//NOI18N
        } catch (Exception e){
            // System.out.println("----- lacking app server classes");
            // e.printStackTrace();
            
        }
        // Empty children. csannot be LEAF: I spent 2 days on this finding...
        return new Children.Keys(){
             protected  Node[] createNodes (Object key){
                 return new Node[0];
        }
        };
        
    }

//    public void propertyChange(java.beans.PropertyChangeEvent evt) {
//        //System.out.println("propertyChange WebServicesRootNodeNetBeansSide");
//        setChildren(createChildrenNodes());// test before doing that
//        if(realNode != null) {
//            setDisplayName(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services"));
//            setShortDescription(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services"));
//        } else {
//            setDisplayName(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services_Disabled"));
//            setShortDescription(NbBundle.getMessage(WebServicesRootNodeNetBeansSide.class, "Web_Services_Disabled_Desc"));
//        }
//    }
}
