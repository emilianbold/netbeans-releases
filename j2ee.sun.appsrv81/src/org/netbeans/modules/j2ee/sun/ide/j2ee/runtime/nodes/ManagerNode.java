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
/*
 * ManagerNode.java
 *
 * Created on December 21, 2003, 8:29 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import javax.enterprise.deploy.spi.DeploymentManager;

import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ViewLogAction;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ShowAdminToolAction;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformFactory;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Customizer;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.TargetServerData;

import java.util.Collection;

/**
 *
 * @author  ludo
 */
public class ManagerNode extends AbstractNode implements Node.Cookie{
    static java.util.Collection bogusNodes = java.util.Arrays.asList(new Node[] { Node.EMPTY, Node.EMPTY });
    private SunDeploymentManagerInterface sdm;
    private DeploymentManager manager;
    private DeploymentManagerProperties dmp;
    public static final String DIR_ACTION_EXTENSION = "/J2EE/SunAppServer/Actions"; //NOI18N
    
    public ManagerNode(DeploymentManager manager) {
        super(new MyChildren(bogusNodes));
        sdm = (SunDeploymentManagerInterface)manager;
        this.manager = manager;
        setDisplayName(sdm.getHost()+":"+sdm.getPort());
        
        setIconBaseWithExtension("org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.png");//NOI18N
        setShortDescription(NbBundle.getMessage(ManagerNode.class, "HINT_node")+" "+sdm.getHost()+":"+sdm.getPort());
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
        PlatformFactory fact = new PlatformFactory();
        J2eePlatformImpl platform = fact.getJ2eePlatformImpl(manager);
        TargetServerData foo = new TargetServerData();
        DeploymentManagerProperties dmp = new DeploymentManagerProperties(manager);
        return new Customizer(platform, dmp);
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.getDefaultFileSystem().findResource(DIR_ACTION_EXTENSION);
        int nbextraoptions=0;
        FileObject[] ch =null;
        if(dir!=null){
            ch = dir.getChildren();
            nbextraoptions = ch.length + 1; 
        }
        javax.swing.Action[]  newActions = new javax.swing.Action[4 + nbextraoptions] ;// 5 hardcoded number of actionns!!
        int a=0;
        newActions[a++]=(null);        
        newActions[a++]= (SystemAction.get(ShowAdminToolAction.class));
        newActions[a++]=(SystemAction.get(ViewLogAction.class));
        
        for(int i = 0; i < nbextraoptions; i++) {
            try{
                DataObject dobj = DataObject.find(ch[i]);
                InstanceCookie cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                if(cookie == null) {
                    System.out.println("error: null cookie for " +ch[i]);
                } else{
                    Class theActionClass = cookie.instanceClass();
                    newActions[a+i]=(SystemAction.get(theActionClass));
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
