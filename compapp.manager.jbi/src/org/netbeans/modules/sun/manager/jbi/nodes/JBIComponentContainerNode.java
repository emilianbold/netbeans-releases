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
import org.netbeans.modules.sun.manager.jbi.actions.InstallAction;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.util.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.JarFileFilter;
import org.netbeans.modules.sun.manager.jbi.util.NodeTypes;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

/**
 * Container node for all JBI Components of the same type.
 *
 * @author jqian
 */
public abstract class JBIComponentContainerNode extends AppserverJBIMgmtContainerNode
        implements Installable {
        
    private static String lastInstallDir = null;
    
    private boolean busy;
    
    public JBIComponentContainerNode(final AppserverJBIMgmtController controller,
            final String type, final String name) {
        super(controller, type);
        setDisplayName(name);
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
            SystemAction.get(getInstallActionClass()),
            SystemAction.get(RefreshAction.class),
        };
    }
    
    /**
     *
     */
    public Image getIcon(int type) {
        String iconName = IconConstants.FOLDER_ICON;
        String badgeIconName = getBadgeIconName();
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
    
    protected Map getSheetProperties() {
        return null;
    }
    
    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }
    
    /**
     * Installs new JBI Component(s).
     */
    public void install() {
        
        AdministrationService adminService =
                getAppserverJBIMgmtController().getJBIAdministrationService();
        
        if (adminService != null) {
            JFileChooser chooser = getJFileChooser();
            int returnValue = chooser.showDialog(null,
                    NbBundle.getMessage(JBIComponentContainerNode.class, "LBL_Install_JBI_Component_Button")); //NOI18N
            
            if (returnValue == JFileChooser.APPROVE_OPTION){
                File[] selectedFiles = chooser.getSelectedFiles();
                
                String progressLabel = getInstallProgressMessageLabel();
                String message = NbBundle.getMessage(JBIComponentContainerNode.class, progressLabel);
                final ProgressUI progressUI = new ProgressUI(message, false);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setBusy(true);
                        progressUI.start();
                    }
                });
                
                for (int i = 0; i < selectedFiles.length; i++) {
                    final String jarFilePath = selectedFiles[i].getAbsolutePath();
                    final String result = installJBIComponent(jarFilePath);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                    GenericConstants.INSTALL_COMPONENT_OPERATION_NAME,
                                    jarFilePath, result);
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
    
    protected AdministrationService getJBIAdministrationService() {
        return getAppserverJBIMgmtController().getJBIAdministrationService();
    }
    
    private JFileChooser getJFileChooser(){
        JFileChooser chooser = new JFileChooser();
        
        ResourceBundle bundle = NbBundle.getBundle(JBIComponentContainerNode.class);
        
        String titleLabel = getFileChooserTitleLabel();
        chooser.setDialogTitle(bundle.getString(titleLabel));
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        
        chooser.setApproveButtonMnemonic(
                bundle.getString("Install_JBI_Component_Button_Mnemonic").charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(JarFileFilter.getInstance());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(
                bundle.getString("LBL_Install_JBI_Component_Button")); //NOI18N
        
        chooser.getAccessibleContext().setAccessibleName(
                bundle.getString(titleLabel));
        chooser.getAccessibleContext().setAccessibleDescription(
                bundle.getString(titleLabel));
        
        if (lastInstallDir != null) {
            chooser.setCurrentDirectory(new File(lastInstallDir));
        }
        
        return chooser;
    }
    
    protected abstract Class getInstallActionClass();
    
    protected abstract String installJBIComponent(String jarFilePath);
    
    protected abstract String getFileChooserTitleLabel();
    
    protected abstract String getInstallProgressMessageLabel();
    
    protected abstract String getBadgeIconName();
    
    //==========================================================================
    
    
    /**
     * Container node for all JBI Service Engines.
     */
    public static class ServiceEngines extends JBIComponentContainerNode {
        
        public ServiceEngines(final AppserverJBIMgmtController controller) {
            super(controller,
                    NodeTypes.SERVICE_ENGINES,
                    NbBundle.getMessage(JBIComponentContainerNode.class, "SERVICE_ENGINES"));    // NOI18N
        }
        
        protected Class getInstallActionClass() {
            return InstallAction.ServiceEngine.class;
        }
        
        protected String installJBIComponent(String jarFilePath) {
            AdministrationService adminService = getJBIAdministrationService();
            return adminService.installComponent(jarFilePath);
        }
        
        protected String getFileChooserTitleLabel() {
            return "LBL_Install_Service_Engine_Chooser_Name";      // NOI18N
        }
        
        protected String getInstallProgressMessageLabel() {
            return "LBL_Installing_Service_Engine";     // NOI18N
        }
        
        protected String getBadgeIconName() {
            return IconConstants.SERVICE_ENGINES_BADGE_ICON;
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
    }
    
    
    /**
     * Container node for all JBI Binding Components.
     */
    public static class BindingComponents extends JBIComponentContainerNode {
        
        public BindingComponents(final AppserverJBIMgmtController controller) {
            super(controller,
                    NodeTypes.BINDING_COMPONENTS,
                    NbBundle.getMessage(JBIComponentContainerNode.class, "BINDING_COMPONENTS")); // NOI18N
        }
        
        protected Class getInstallActionClass() {
            return InstallAction.BindingComponent.class;
        }
        
        protected String installJBIComponent(String jarFilePath) {
            AdministrationService adminService = getJBIAdministrationService();
            return adminService.installComponent(jarFilePath);
        }
        
        protected String getFileChooserTitleLabel() {
            return "LBL_Install_Binding_Component_Chooser_Name";      // NOI18N
        }
        
        protected String getInstallProgressMessageLabel() {
            return "LBL_Installing_Binding_Component";     // NOI18N
        }
        
        protected String getBadgeIconName() {
            return IconConstants.BINDING_COMPONENTS_BADGE_ICON;
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
    }
    
    
    /**
     * Container node for all JBI Shared Libraries.
     */
    public static class SharedLibraries extends JBIComponentContainerNode {
        
        public SharedLibraries(final AppserverJBIMgmtController controller) {
            super(controller,
                    NodeTypes.SHARED_LIBRARIES,
                    NbBundle.getMessage(JBIComponentContainerNode.class, "SHARED_LIBRARIES"));   // NOI18N
        }
        
        protected Class getInstallActionClass() {
            return InstallAction.SharedLibrary.class;
        }
        
        protected String installJBIComponent(String jarFilePath) {
            AdministrationService adminService = getJBIAdministrationService();
            return adminService.installSharedLibrary(jarFilePath);
        }
        
        protected String getFileChooserTitleLabel() {
            return "LBL_Install_Shared_Library_Chooser_Name";      // NOI18N
        }
        
        protected String getInstallProgressMessageLabel() {
            return "LBL_Installing_Shared_Library";     // NOI18N
        }
        
        protected String getBadgeIconName() {
            return IconConstants.SHARED_LIBRARIES_BADGE_ICON;
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
    }
}
