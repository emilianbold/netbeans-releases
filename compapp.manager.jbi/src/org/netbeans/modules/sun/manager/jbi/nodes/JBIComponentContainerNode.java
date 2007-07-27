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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.netbeans.modules.sun.manager.jbi.actions.InstallAction;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.FileFilters;
import org.netbeans.modules.sun.manager.jbi.util.NodeTypes;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
            String type, String name) {
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
    
    public void refresh() {
        // clear the cache first
        AdministrationService adminService = getAdminService();
        adminService.clearJBIComponentStatusCache(getComponentType());
        
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
    
    protected Map<Attribute, MBeanAttributeInfo> getSheetProperties() {
        return null;
    }
    
    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }
        
    /**
     * Installs new JBI Component(s).
     */
    public void install() {
        
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            JFileChooser chooser = getJFileChooser();
            int returnValue = chooser.showDialog(null,
                    NbBundle.getMessage(JBIComponentContainerNode.class, 
                    "LBL_Install_JBI_Component_Button")); //NOI18N
            
            if (returnValue == JFileChooser.APPROVE_OPTION){
                File[] selectedFiles = chooser.getSelectedFiles();                
                if (selectedFiles.length > 0) {
                    lastInstallDir = selectedFiles[0].getParent();
                }
                
                List<File> files = filterSelectedFiles(selectedFiles);                
                if (files.size() == 0) {
                    return;
                }
                
                String progressLabel = getInstallProgressMessageLabel();
                String message = NbBundle.getMessage(JBIComponentContainerNode.class, progressLabel);
                final ProgressUI progressUI = new ProgressUI(message, false);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setBusy(true);
                        progressUI.start();
                    }
                });
                
                for (File file : files) {
                    final String jarFilePath = file.getAbsolutePath();
                    final String result = installJBIComponent(jarFilePath);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                    GenericConstants.INSTALL_COMPONENT_OPERATION_NAME,
                                    jarFilePath, result);
                        }
                    });
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
            for (File file : files) {
                // do some simple validation
                boolean isRightType = false;
                
                JarFile jf = null;
                try {
                    jf = new JarFile(file); 
                    JarEntry je = (JarEntry) jf.getEntry("META-INF/jbi.xml"); // NOI18N
                    if (je != null) {
                        InputStream is = jf.getInputStream(je);
                        Document doc = docBuilder.parse(is);
                        isRightType = isRightJBIDocType(doc); // very basic type checking
                    }                    
                } catch (Exception e) {
                    e.printStackTrace(); 
                } finally {
                    if (jf != null) {
                        try {
                            jf.close();
                        } catch (IOException e) {
                            ; // ignore
                        }
                    }                    
                }                
                
                if (isRightType) {
                    ret.add(file);
                } else {
                    String compType = NbBundle.getMessage(
                            getClass(),
                            getComponentTypeLabel());
                    String msg = NbBundle.getMessage(
                            getClass(),
                            "MSG_INVALID_COMPONENT_INSTALLATION", // NOI18N
                            file.getName(),
                            compType);
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            msg,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        }
        
        return ret;
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
        
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.addChoosableFileFilter(FileFilters.JarFileFilter.getInstance());        
        
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
    
    protected abstract boolean isRightJBIDocType(Document jbiDoc);
    
    protected abstract String getComponentTypeLabel();
    
    protected abstract String getComponentType();
    
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
            AdministrationService adminService = getAdminService();
            return adminService.installComponent(jarFilePath);
        }
        
        protected boolean isRightJBIDocType(Document jbiDoc) {
            NodeList ns = jbiDoc.getElementsByTagName("component"); // NOI18N
            if (ns.getLength() > 0) {
                Element e = (Element) ns.item(0);
                String type = e.getAttribute("type"); // NOI18N
                if (type != null && type.equals("service-engine")) {    // NOI18N 
                    return true;
                }
            }
            return false;
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
        
        protected String getComponentTypeLabel() {
            return "SERVICE_ENGINE";    // NOI18N
        }
        
        protected String getComponentType() {
            return JBIComponentStatus.ENGINE_TYPE;    
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
            AdministrationService adminService = getAdminService();
            return adminService.installComponent(jarFilePath);
        }
                
        protected boolean isRightJBIDocType(Document jbiDoc) {            
            NodeList ns = jbiDoc.getElementsByTagName("component"); // NOI18N
            if (ns.getLength() > 0) {
                Element e = (Element) ns.item(0);
                String type = e.getAttribute("type"); // NOI18N
                if (type != null && type.equals("binding-component")) {    // NOI18N // FIXME
                    return true;
                }
            }
            return false;
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
                
        protected String getComponentTypeLabel() {
            return "BINDING_COMPONENT";    // NOI18N
        }
        
        protected String getComponentType() {
            return JBIComponentStatus.BINDING_TYPE;    
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
            AdministrationService adminService = getAdminService();
            return adminService.installSharedLibrary(jarFilePath);
        }        
        
        protected boolean isRightJBIDocType(Document jbiDoc) {            
            NodeList ns = jbiDoc.getElementsByTagName("shared-library"); // NOI18N
            return ns.getLength() > 0;
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
        
        protected String getComponentTypeLabel() {
            return "SHARED_LIBRARY";    // NOI18N
        }
        
        protected String getComponentType() {
            return JBIComponentStatus.NAMESPACE_TYPE;    
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
    }
}
