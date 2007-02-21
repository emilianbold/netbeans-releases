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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sun.manager.jbi.nodes;

import java.awt.Image;
import java.io.File;
import java.util.Map;
import java.util.ResourceBundle;

import javax.management.Attribute;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.netbeans.modules.sun.manager.jbi.actions.DeployAction;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.util.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.NodeTypes;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.netbeans.modules.sun.manager.jbi.util.ZipFileFilter;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

/**
 * Container node for all JBI Service Assemblies.
 *
 * @author jqian
 */
public class JBIServiceAssembliesNode extends AppserverJBIMgmtContainerNode
        implements Deployable {
    
    private static final String NODE_TYPE = NodeTypes.SERVICE_ASSEMBLIES;
    
    private static String lastInstallDir = null;
        
    private boolean busy;
    
    
    public JBIServiceAssembliesNode(final AppserverJBIMgmtController controller) {
        super(controller, NODE_TYPE);
        
        setDisplayName(NbBundle.getMessage(JBIServiceAssembliesNode.class, "SERVICE_ASSEMBLIES"));  // NOI18N
    }
    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on a node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {
            SystemAction.get(DeployAction.class),
            SystemAction.get(RefreshAction.class),
        };
    }
    
    /**
     *
     */
    public Image getIcon(int type) {
        String iconName = IconConstants.FOLDER_ICON;
        String badgeIconName = IconConstants.SERVICE_ASSEMBLIES_BADGE_ICON;
        String externalBadgeIconName = busy ? IconConstants.BUSY_ICON : null;
        return Utils.getBadgedIcon(getClass(), iconName, badgeIconName, externalBadgeIconName);
    }
    
    /**
     *
     */
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    /**
     *
     * @param busy
     */
    private void setBusy(boolean busy) {
        this.busy = busy;
        fireIconChange();
    }
    
    
    /**
     * Deploys new Service Assemblies.
     */
    public void deploy() {
        
        AdministrationService adminService =
                getAppserverJBIMgmtController().getJBIAdministrationService();
        
        if (adminService != null) {
            
            JFileChooser chooser = getJFileChooser();
            
            int returnValue = chooser.showDialog(null,
                    NbBundle.getMessage(JBIServiceAssembliesNode.class, "LBL_Deploy_Service_Assembly_Button"));   //NOI18N
            
            if (returnValue == JFileChooser.APPROVE_OPTION){
                File[] selectedFiles = chooser.getSelectedFiles();
                
                String message =
                        NbBundle.getMessage(JBIServiceAssembliesNode.class, "LBL_Deploying_Service_Assembly");    // NOI18N
                final ProgressUI progressUI = new ProgressUI(message, false);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setBusy(true);
                        progressUI.start();
                    }
                });
                
                for (int i = 0; i < selectedFiles.length; i++) {
                    final String zipFilePath = selectedFiles[i].getAbsolutePath();
                    final String result = adminService.deployServiceAssembly(zipFilePath);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                    GenericConstants.DEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                                    zipFilePath, result);
                        }
                    });
                    
                    if (i == 0) {
                        lastInstallDir = selectedFiles[0].getParent();
                    }
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        progressUI.finish();
                        setBusy(false);
                    }
                });
            }
        }
    }
    
    protected Map getSheetProperties() {
        return null;
    }
    
    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }
    
    private JFileChooser getJFileChooser(){
        JFileChooser chooser = new JFileChooser();
        
        ResourceBundle bundle = NbBundle.getBundle(JBIComponentContainerNode.class);
        
        chooser.setDialogTitle(
                bundle.getString("LBL_Deploy_Service_Assembly_Chooser_Name")); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        chooser.setApproveButtonMnemonic(
                bundle.getString("Deploy_Service_Assembly_Button_Mnemonic").charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(ZipFileFilter.getInstance());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(
                bundle.getString("LBL_Deploy_Service_Assembly_Button")); //NOI18N
        chooser.getAccessibleContext().setAccessibleName(
                bundle.getString("LBL_Deploy_Service_Assembly_Chooser_Name")); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription(
                bundle.getString("LBL_Deploy_Service_Assembly_Chooser_Name")); //NOI18N
        
        if (lastInstallDir != null) {
            chooser.setCurrentDirectory(new File(lastInstallDir));
        }
        
        return chooser;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JBIServiceAssembliesNode.class);
    }
}
