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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi.nodes;

import com.sun.esb.management.api.deployment.DeploymentService;
import com.sun.esb.management.common.ManagementRemoteException;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.util.FileFilters;
import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.netbeans.modules.sun.manager.jbi.actions.DeployAction;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    
    private static String lastInstallDir = null;
        
    private boolean busy;
    
    
    public JBIServiceAssembliesNode(final AppserverJBIMgmtController controller) {
        super(controller, NodeType.SERVICE_ASSEMBLIES);
        
        setDisplayName(NbBundle.getMessage(JBIServiceAssembliesNode.class, 
                "SERVICE_ASSEMBLIES"));  // NOI18N
    }
    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on a node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    @Override
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {
            SystemAction.get(DeployAction.DeployOnly.class),
            SystemAction.get(DeployAction.DeployAndStart.class),
            null,
            SystemAction.get(RefreshAction.class),
        };
    }
    
    /**
     *
     */
    @Override
    public Image getIcon(int type) {
        String iconName = IconConstants.FOLDER_ICON;
        String badgeIconName = IconConstants.SERVICE_ASSEMBLIES_BADGE_ICON;
        String externalBadgeIconName = busy ? IconConstants.BUSY_ICON : null;
        return Utils.getBadgedIcon(getClass(), iconName, badgeIconName, externalBadgeIconName);
    }
    
    /**
     *
     */
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    @Override
    public void refresh() {
        // clear the cache first
        RuntimeManagementServiceWrapper mgmtService = 
                getRuntimeManagementServiceWrapper();
        
        mgmtService.clearServiceAssemblyStatusCache();
        
        super.refresh();
    }
    
    /**
     *
     * @param busy
     */
    private void setBusy(boolean busy) {
        this.busy = busy;
        fireIconChange();
    }    
    
    public void deploy(boolean start) {
        
        DeploymentService deploymentService = getDeploymentService();
        assert deploymentService != null;
            
        JFileChooser chooser = getJFileChooser();

        int returnValue = chooser.showDialog(null,
                NbBundle.getMessage(JBIServiceAssembliesNode.class, 
                "LBL_Deploy_Service_Assembly_Button"));   //NOI18N

        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] selectedFiles = chooser.getSelectedFiles();
        if (selectedFiles.length > 0) {
            lastInstallDir = selectedFiles[0].getParent();
        }

        List<File> files = filterSelectedFiles(selectedFiles);                
        if (files.size() == 0) {
            return;
        }

        String message = NbBundle.getMessage(JBIServiceAssembliesNode.class, 
                "LBL_Deploying_Service_Assembly");    // NOI18N
        final ProgressUI progressUI = new ProgressUI(message, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        for (File file : files) {
            final String zipFilePath = file.getAbsolutePath();

            try {
                String result = deploymentService.deployServiceAssembly(
                        zipFilePath, SERVER_TARGET);
                assert result != null;
             
                String lowerCaseResult = result.toLowerCase();
                if (!lowerCaseResult.contains("error") && // NOI18N
                        !lowerCaseResult.contains("warning") && // NOI18N
                        !lowerCaseResult.contains("exception") && // NOI18N
                        !lowerCaseResult.contains("info")) {     // NOI18N
                    if (start) {
                        // Start component automatically only upon 
                        // successful installation.
                        // The successful installation result is the 
                        // service assembly name.
                        String assembly = result;
                        RuntimeManagementServiceWrapper mgmtService =
                                getRuntimeManagementServiceWrapper();
                        result = mgmtService.startServiceAssembly(
                                assembly, SERVER_TARGET);
                        
                        JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                GenericConstants.START_SERVICE_ASSEMBLY_OPERATION_NAME,
                                assembly, result);
                    }
                } else {
                    // Failed to deploy
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.DEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                            zipFilePath, result);                    
                }
            } catch (ManagementRemoteException e) {
                JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.DEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                            zipFilePath, e.getMessage());   
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressUI.finish();
                setBusy(false);
            }
        });
    }
    
    protected Map<Attribute, MBeanAttributeInfo> getGeneralSheetSetProperties() {
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
        
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.addChoosableFileFilter(FileFilters.ArchiveFileFilter.getInstance());
        
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
    
    private List<File> filterSelectedFiles(File[] files) {
        List<File> ret = new ArrayList<File>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
        
        if (docBuilder != null) {
            JBIArtifactValidator validator = 
                    JBIArtifactValidator.getServiceAssemblyValidator();
            for (File file : files) {               
                if (validator.validate(file)) {
                    ret.add(file);
                } else {
                    String msg = NbBundle.getMessage(
                            getClass(),
                            "MSG_INVALID_SERVICE_ASSEMBLY_DEPLOYMENT", // NOI18N
                            file.getName());
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            msg,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        }
        
        return ret;
    }   
    
    protected boolean needRefresh(String notificationSourceType) {
        return notificationSourceType.equals("ServiceAssembly"); // NOI18N
    }
        
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JBIServiceAssembliesNode.class);
    }
}
