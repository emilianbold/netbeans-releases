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
/*
 * RegisterAction.java
 *
 * Created on February 2, 2005, 12:01 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.resourceactions;

import java.text.MessageFormat;
import javax.swing.SwingUtilities;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.actions.NodeAction;

import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ListServerInstances;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;
/**
 *
 * @author Nitya Doraisamy
 */
public class RegisterAction extends NodeAction implements WizardConstants{

    protected void performAction(Node[] nodes) {
        try{
            SunResourceDataObject dobj = (SunResourceDataObject)nodes[0].getCookie(SunResourceDataObject.class);
            String resourceType = dobj.getResourceType();
            if(resourceType != null){
                InstanceProperties target = getTargetServer(nodes[0]);
                new ListServerInstances(NbBundle.getMessage(RegisterAction.class, ("Reg_" + resourceType)), dobj, resourceType, target); //NOI18N
            }else{
                String message = MessageFormat.format(NbBundle.getMessage(RegisterAction.class, "Err_InvalidXML"), new Object[]{nodes[0].getName()}); //NOI18N 
                showError(message);
            }
        }catch(Exception ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
        return "org/netbeans/modules/j2ee/sun/ide/resources/AddInstanceActionIcon.gif"; //NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RegisterAction.class);
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
    
    public static void showError(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RegisterAction.class, "HINT_Action"));
     * }
     */
    
}
