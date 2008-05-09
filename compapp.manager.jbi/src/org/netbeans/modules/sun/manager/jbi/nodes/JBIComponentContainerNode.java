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

import com.sun.esb.management.api.installation.InstallationService;
import com.sun.esb.management.common.ManagementRemoteException;
import java.awt.Frame;
import java.io.IOException;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
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
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationParser;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.util.FileFilters;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.windows.WindowManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Container node for all the JBI Components (SE, BC, or Shared Library) 
 * of the same type.
 *
 * @author jqian
 */
public abstract class JBIComponentContainerNode
        extends AppserverJBIMgmtContainerNode implements Installable {

    private boolean busy;
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.sun.manager.jbi.node.JBIComponentContainerNode"); // NOI18N


    public JBIComponentContainerNode(final AppserverJBIMgmtController controller,
            NodeType type, String name) {
        super(controller, type);
        setDisplayName(name);
    }

    @Override
    public Image getIcon(int type) {
        String iconName = IconConstants.FOLDER_ICON;
        String badgeIconName = getBadgeIconName();
        String externalBadgeIconName = busy ? IconConstants.BUSY_ICON : null;
        return Utils.getBadgedIcon(getClass(), iconName, badgeIconName, externalBadgeIconName);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public void refresh() {
        // clear the cache first
        RuntimeManagementServiceWrapper mgmtService = getRuntimeManagementServiceWrapper();
        mgmtService.clearJBIComponentStatusCache(getComponentType());

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

    protected Map<Attribute, MBeanAttributeInfo> getGeneralSheetSetProperties() {
        return null;
    }

    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }

    public void install(boolean start) {

        InstallationService installationService = getInstallationService();
        assert installationService != null;

        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(null,
                NbBundle.getMessage(JBIComponentContainerNode.class,
                "LBL_Install_JBI_Component_Button")); //NOI18N

        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] selectedFiles = chooser.getSelectedFiles();
        if (selectedFiles.length > 0) {
            System.setProperty(
                    JBIComponentNode.LAST_JBI_COMPONENT_INSTALLATION_DIRECTORY,
                    selectedFiles[0].getParent());
        }

        List<File> files = filterSelectedFiles(selectedFiles);
        if (files.size() == 0) {
            return;
        }

        String progressLabel = getInstallProgressMessageLabel();
        String message = NbBundle.getMessage(
                JBIComponentContainerNode.class, progressLabel);
        final ProgressUI progressUI = new ProgressUI(message, false);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        for (File file : files) {
            final String jarFilePath = file.getAbsolutePath();

            try {
                String result = installComponent(jarFilePath);

                if (result != null) {
                    String lowerCaseResult = result.toLowerCase();
                    if (!lowerCaseResult.contains("error") && // NOI18N
                            !lowerCaseResult.contains("warning") && // NOI18N
                            !lowerCaseResult.contains("exception") &&// NOI18N
                            !lowerCaseResult.contains("info")) {     // NOI18N
                        if (start) {
                            // Start component automatically only upon 
                            // successful installation.
                            // The successful installation result is the 
                            // component ID.
                            try {
                                String componentID = result;
                                RuntimeManagementServiceWrapper mgmtService =
                                        getRuntimeManagementServiceWrapper();
                                result = mgmtService.startComponent(
                                        componentID, SERVER_TARGET);

                                JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                        GenericConstants.START_COMPONENT_OPERATION_NAME,
                                        componentID, result);
                            } catch (ManagementRemoteException e) {
                                JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                        GenericConstants.START_COMPONENT_OPERATION_NAME,
                                        jarFilePath, e.getMessage());
                            }
                        }
                    } else {
                        // Failed to install
                        JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                GenericConstants.INSTALL_COMPONENT_OPERATION_NAME,
                                jarFilePath, result);
                    }
                }
            } catch (ManagementRemoteException e) {
                JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                        GenericConstants.INSTALL_COMPONENT_OPERATION_NAME,
                        jarFilePath, e.getMessage());
            }
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                progressUI.finish();
                setBusy(false);
            }
        });
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
            JBIArtifactValidator validator = getValidator();
            for (File file : files) {
                if (validator.validate(file)) {
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

    private JFileChooser getJFileChooser() {
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

        String lastInstallDir = System.getProperty(
                JBIComponentNode.LAST_JBI_COMPONENT_INSTALLATION_DIRECTORY);
        if (lastInstallDir != null) {
            chooser.setCurrentDirectory(new File(lastInstallDir));
        }

        return chooser;
    }

    /**
     * Installs the given JBI Component or Shared Library.
     * 
     * @param jarFilePath   absolute path for the component jar file
     * @return  the component ID if the installation is successful; 
     *          or the error string in XML if the installation is failed;
     *          or <code>null</code> if the component installation is cancelled.
     * @throws com.sun.esb.management.common.ManagementRemoteException
     */
    protected abstract String installComponent(String jarFilePath)
            throws ManagementRemoteException;

    protected abstract String getFileChooserTitleLabel();

    protected abstract String getInstallProgressMessageLabel();

    protected abstract String getBadgeIconName();

    protected abstract JBIArtifactValidator getValidator();

    protected abstract String getComponentTypeLabel();

    protected abstract JBIComponentType getComponentType();

    //==========================================================================
    /**
     * Container node for all Service Engines or Binding Components.
     */
    static abstract class RealJBIComponentContainerNode
            extends JBIComponentContainerNode {

        RealJBIComponentContainerNode(final AppserverJBIMgmtController controller,
                NodeType type, String name) {
            super(controller, type, name);
        }

        @Override
        public Action[] getActions(boolean flag) {
            return new SystemAction[]{
                SystemAction.get(InstallAction.InstallOnly.class),
                SystemAction.get(InstallAction.InstallAndStart.class),
                null,
                SystemAction.get(RefreshAction.class)
            };
        }

        protected String installComponent(String jarFilePath)
                throws ManagementRemoteException {
            Properties properties = new Properties();

            InstallationService installationService = getInstallationService();

            try {
                // Get jbi.xml from component jar file
                JarFile jarFile = new JarFile(jarFilePath);
                ZipEntry jbiEntry = jarFile.getEntry("META-INF/jbi.xml");
                InputStream is = jarFile.getInputStream(jbiEntry);
                String jbiXml = getContent(is);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document jbiDoc = builder.parse(
                        new InputSource(new StringReader(jbiXml)));

                // Get configuration descriptor from jbi.xml
                final JBIComponentConfigurationDescriptor rootDescriptor =
                        JBIComponentConfigurationParser.parse(jbiDoc);
                if (rootDescriptor != null && rootDescriptor.showDisplayAtInstallation(true)) {
                    String componentName = getComponentIDFromJbiDoc(jbiDoc);

                    // Show installation configuration dialog.
                    JBIComponentInstallationConfigurationDialog dialog =
                            new JBIComponentInstallationConfigurationDialog(
                            componentName, rootDescriptor);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                    if (dialog.isCancelled()) {
                        return null;
                    }

                    properties = dialog.getProperties();

                    logger.info("Install " + componentName + // NOI18N
                            " with these properties:" + properties); // NOI18N
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                NotifyDescriptor d = new NotifyDescriptor.Message(ex.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }

            return installationService.installComponent(
                    jarFilePath, properties, SERVER_TARGET);
        }

        private String getComponentIDFromJbiDoc(Document jbiDoc) {
            NodeList ns = jbiDoc.getElementsByTagName("component"); // NOI18N
            if (ns.getLength() > 0) {
                Element e = (Element) ns.item(0);
                NodeList ids = e.getElementsByTagName("identification"); // NOI18N
                if (ids.getLength() > 0) {
                    Element id = (Element) ids.item(0);
                    NodeList names = id.getElementsByTagName("name"); // NOI18N
                    if (names.getLength() > 0) {
                        Element n = (Element) names.item(0);
                        return n.getFirstChild().getNodeValue();
                    }
                }
            }
            
            return null;
        }

        private String getContent(InputStream is) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        private String getContent(File file) {
            String ret = "";

            BufferedReader is = null;
            try {
                is = new BufferedReader(new FileReader(file));
                String inputLine;
                while ((inputLine = is.readLine()) != null) {
                    ret += inputLine;
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                }
            }

            return ret;
        }
    }

    /**
     * Container node for all JBI Service Engines.
     */
    static class ServiceEngines extends RealJBIComponentContainerNode {

        ServiceEngines(final AppserverJBIMgmtController controller) {
            super(controller,
                    NodeType.SERVICE_ENGINES,
                    NbBundle.getMessage(JBIComponentContainerNode.class,
                    "SERVICE_ENGINES"));    // NOI18N
        }

        protected JBIArtifactValidator getValidator() {
            return JBIArtifactValidator.getServiceEngineValidator(null);
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

        protected JBIComponentType getComponentType() {
            return JBIComponentType.SERVICE_ENGINE;
        }
        
        protected boolean needRefresh(String notificationSourceType) {
            return notificationSourceType.equals("ServiceEngine"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
    }

    /**
     * Container node for all JBI Binding Components.
     */
    static class BindingComponents extends RealJBIComponentContainerNode {

        BindingComponents(final AppserverJBIMgmtController controller) {
            super(controller,
                    NodeType.BINDING_COMPONENTS,
                    NbBundle.getMessage(JBIComponentContainerNode.class,
                    "BINDING_COMPONENTS")); // NOI18N
        }

        protected JBIArtifactValidator getValidator() {
            return JBIArtifactValidator.getBindingComponentValidator(null);
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

        protected JBIComponentType getComponentType() {
            return JBIComponentType.BINDING_COMPONENT;
        }
        
        protected boolean needRefresh(String notificationSourceType) {
            return notificationSourceType.equals("BindingComponent"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
    }

    /**
     * Container node for all JBI Shared Libraries.
     */
    static class SharedLibraries extends JBIComponentContainerNode {

        SharedLibraries(final AppserverJBIMgmtController controller) {
            super(controller,
                    NodeType.SHARED_LIBRARIES,
                    NbBundle.getMessage(JBIComponentContainerNode.class,
                    "SHARED_LIBRARIES"));   // NOI18N
        }

        @Override
        public Action[] getActions(boolean flag) {
            return new SystemAction[]{
                SystemAction.get(InstallAction.InstallOnly.class),
                null,
                SystemAction.get(RefreshAction.class)
            };
        }

        protected String installComponent(
                String jarFilePath)
                throws ManagementRemoteException {
            InstallationService installationService = getInstallationService();
            return installationService.installSharedLibrary(jarFilePath, SERVER_TARGET);
        }

        protected JBIArtifactValidator getValidator() {
            return JBIArtifactValidator.getSharedLibraryValidator();
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

        protected JBIComponentType getComponentType() {
            return JBIComponentType.SHARED_LIBRARY;
        }
        
        protected boolean needRefresh(String notificationSourceType) {
            return notificationSourceType.equals("SharedLibrary"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
    }
}
