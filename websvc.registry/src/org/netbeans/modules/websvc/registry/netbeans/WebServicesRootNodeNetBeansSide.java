/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.netbeans;


import java.io.File;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
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
            return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup.png");
        else 
            return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup_invalid.png");
    }
    
    public Image getOpenedIcon(int type){
        if (realNode!=null)
            return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup.png");
        else
            return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservicegroup_invalid.png");
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
