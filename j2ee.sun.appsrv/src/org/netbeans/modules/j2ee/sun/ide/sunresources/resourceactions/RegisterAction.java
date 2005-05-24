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
 * RegisterAction.java
 *
 * Created on February 2, 2005, 12:01 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.resourceactions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node.PropertySet;

import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ListServerInstances;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
/**
 *
 * @author Nitya Doraisamy
 */
public class RegisterAction extends NodeAction implements WizardConstants{
    String resourceType = null;
    protected void performAction(Node[] nodes) {
        try{
            resourceType = nodes[0].getValue(__ResourceType).toString();            
            PropertySet[] props = nodes[0].getPropertySets();
            InstanceProperties target = getTargetServer(nodes[0]);
            new ListServerInstances(NbBundle.getMessage (RegisterAction.class, ("Reg_" + resourceType)), props[0].getProperties(), resourceType, nodes[0].getName(), target); //NOI18N
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    protected boolean enable(Node[] nodes) {
       if( (nodes != null) && (nodes.length == 1) )
            return true;
        else
            return false;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(RegisterAction.class, "LBL_RegisterAction"); //NOI18N
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/sun/ide/resources/RegisterConnPoolActionIcon.gif"; //NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RegisterConnPoolAction.class);
    }
    
    private InstanceProperties getTargetServer(Node node){
        InstanceProperties serverName = null;
        DataObject dob = (DataObject) node.getCookie(DataObject.class);
        if(dob!=null){
            FileObject fo = dob.getPrimaryFile();
            Project holdingProj = FileOwnerQuery.getOwner(fo);
            if (holdingProj != null){
                J2eeModuleProvider modProvider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
                serverName = modProvider.getInstanceProperties();
            }
        }
        return serverName;
    }
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RegisterConnPoolAction.class, "HINT_Action"));
     * }
     */
    
}
