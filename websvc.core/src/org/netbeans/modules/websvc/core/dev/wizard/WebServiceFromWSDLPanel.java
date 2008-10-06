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
package org.netbeans.modules.websvc.core.dev.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.ServerType;
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.netbeans.modules.websvc.core.dev.wizard.nodes.WsdlNode;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperGenerator;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.xml.sax.SAXException;

/**
 *
 * @author  radko
 */
public class WebServiceFromWSDLPanel extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private static String previousDirectory = "";
    private final FileFilter WSDL_FILE_FILTER = new WsdlFileFilter();
    private boolean isWaitingForScan = false;
    private WsdlModeler wsdlModeler;
    private WsdlModel wsdlModel;
    private WsdlService service;
    private WsdlPort port;
    private WsdlServiceHandler wsdlServiceHandler;
    private Project project;
    private WizardDescriptor wizardDescriptor;
    private JAXWSSupport wss;
    private boolean jsr109Supported;
    private boolean jsr109oldSupported;
    private boolean noMetroInstalledOnGlassFishV3;
    private boolean jaxWsInJ2ee14Supported;
    private WebModule wm;
    private RequestProcessor.Task generateWsdlModelTask;
    private URL wsdlURL;

    /** Creates new form WebServiceFromWSDLPanel */
    public WebServiceFromWSDLPanel(Project project) {
        this.project = project;
        initComponents();
        initJsr109Info();

        jTextFieldWSDLFile.getDocument().addDocumentListener(this);
         
        if(supportsJaxrpc()){
            useProviderBtn.setVisible(false);
        }
        generateWsdlModelTask = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {

                WsdlWrapperHandler handler = null;
                try {
                    handler = WsdlWrapperGenerator.parse(wsdlURL.toExternalForm());
                } catch (ParserConfigurationException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } catch (SAXException ex) {
                    String mes = NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_SAXException", ex.getLocalizedMessage()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                if (handler != null && !handler.isServiceElement()) {
                    StreamSource source = new StreamSource(wsdlURL.toExternalForm());
                    try {
                        File wsdlFile = new File(System.getProperty("java.io.tmpdir"), WsdlWrapperGenerator.getWrapperName(wsdlURL)); //NOI18N

                        if (!wsdlFile.exists()) {
                            try {
                                wsdlFile.createNewFile();
                            } catch (IOException ex) {
                                String mes = NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(desc);
                                return;
                            }
                        }
                        WsdlWrapperGenerator.generateWrapperWSDLContent(wsdlFile, source, handler.getTargetNsPrefix(), wsdlURL.toExternalForm());
                        wsdlURL = wsdlFile.toURL();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }

                final WsdlWrapperHandler wsdlHandler = handler;

                wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlURL);
                wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                    public void modelCreated(WsdlModel model) {
                        wsdlModel = model;
                        if (wsdlModel == null) {
                            String serviceName = null;
                            String portName = null;
                            try {
                                wsdlServiceHandler = WsdlServiceHandler.parse(wsdlURL.toExternalForm());
                                serviceName = wsdlServiceHandler.getServiceName();
                                portName = wsdlServiceHandler.getPortName();
                            } catch (ParserConfigurationException ex) {
                            } catch (SAXException ex) {
                            } catch (IOException ex) {
                            }
                            if (serviceName != null && portName != null) {
                                jTextFieldPort.setText(serviceName + "#" + portName);
                            } else {
                                RequestProcessor.getDefault().post(new Runnable() {

                                    public void run() {
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                                NbBundle.getMessage(WebServiceFromWSDLPanel.class, "TXT_CannotGenerateService",
                                                wsdlModeler.getCreationException().getLocalizedMessage()),
                                                NotifyDescriptor.WARNING_MESSAGE));
                                    }
                                });
                            }
                        } else {
                            jButtonBrowsePort.setEnabled(true);
                            List services = wsdlModel.getServices();
                            if (services != null && services.size() > 0) {
                                service = (WsdlService) services.get(0);
                                List ports = service.getPorts();
                                if (ports != null && ports.size() > 0) {
                                    port = (WsdlPort) ports.get(0);
                                }
                            }
                        }
                        if (service != null && port != null) {
                            jTextFieldPort.setText(service.getName() + "#" + port.getName()); //NOI18N
                            if (wsdlHandler != null) {
                                String bindingType = wsdlHandler.getBindingTypeForPort(port.getName());
                                if (bindingType != null) {
                                    port.setSOAPVersion(bindingType);
                                }
                            }
                        }

                        fireChange(); //refresh wizard buttons
                    }
                });
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelWSDLFile = new javax.swing.JLabel();
        jTextFieldWSDLFile = new javax.swing.JTextField();
        jButtonWSDLFileBrowse = new javax.swing.JButton();
        jLabelPortDescription = new javax.swing.JLabel();
        jLabelPort = new javax.swing.JLabel();
        jTextFieldPort = new javax.swing.JTextField();
        jButtonBrowsePort = new javax.swing.JButton();
        useProviderBtn = new javax.swing.JCheckBox();

        jLabelWSDLFile.setLabelFor(jTextFieldWSDLFile);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelWSDLFile, org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_WSDL_File")); // NOI18N
        jLabelWSDLFile.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "HINT_WSDL_File")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonWSDLFileBrowse, org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_Browse")); // NOI18N
        jButtonWSDLFileBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "HINT_WSDL_File")); // NOI18N
        jButtonWSDLFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWSDLFileBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabelPortDescription, org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("LBL_PortMessage")); // NOI18N

        jLabelPort.setLabelFor(jTextFieldPort);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPort, org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("LBL_WebServicePort")); // NOI18N
        jLabelPort.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "TTL_SelectPort")); // NOI18N

        jTextFieldPort.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowsePort, org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("LBL_BrowsePort")); // NOI18N
        jButtonBrowsePort.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "TTL_SelectPort")); // NOI18N
        jButtonBrowsePort.setEnabled(false);
        jButtonBrowsePort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowsePortActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(useProviderBtn, org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_UseProvider")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelWSDLFile)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextFieldWSDLFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
                            .add(jLabelPortDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(jLabelPort)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextFieldPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonWSDLFileBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonBrowsePort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(useProviderBtn)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelWSDLFile)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonWSDLFileBrowse)
                    .add(jTextFieldWSDLFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelPortDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonBrowsePort)
                    .add(jLabelPort)
                    .add(jTextFieldPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(57, 57, 57)
                .add(useProviderBtn)
                .addContainerGap(122, Short.MAX_VALUE))
        );

        jTextFieldWSDLFile.getAccessibleContext().setAccessibleDescription("null");
        jButtonWSDLFileBrowse.getAccessibleContext().setAccessibleDescription("null");
        jTextFieldPort.getAccessibleContext().setAccessibleDescription("null");
    }// </editor-fold>//GEN-END:initComponents
    private void jButtonBrowsePortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowsePortActionPerformed
        Node root = new WsdlNode(wsdlModel);
        PortChooser chooser = new PortChooser(root);
        final DialogDescriptor dd = new DialogDescriptor(chooser, org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "TTL_SelectPort")); //NOI18N

        chooser.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PortChooser.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dd.setValid(((Boolean) newvalue).booleanValue());
                    }
                }
            }
        });

        Object result = DialogDisplayer.getDefault().notify(dd);

        if (result == DialogDescriptor.OK_OPTION) {
            if (Util.isJavaEE5orHigher(project) || jaxWsInJ2ee14Supported ||
                    (!jsr109Supported && !jsr109oldSupported ||
                    (!jsr109Supported && jsr109oldSupported /* && jwsdpSupported*/))) {
                jTextFieldPort.setText(chooser.getSelectedPortOwnerName() + "#" + chooser.getSelectedNodes()[0].getDisplayName()); //NOI18N
                service = wsdlModel.getServiceByName(chooser.getSelectedPortOwnerName());
                port = service.getPortByName(chooser.getSelectedNodes()[0].getDisplayName());
            }
        }
    }//GEN-LAST:event_jButtonBrowsePortActionPerformed

    private void jButtonWSDLFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWSDLFileBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(previousDirectory);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(WSDL_FILE_FILTER);
        chooser.setFileFilter(WSDL_FILE_FILTER);

        if (chooser.showOpenDialog(WebServiceFromWSDLPanel.this) == JFileChooser.APPROVE_OPTION) {
            File wsdlFile = chooser.getSelectedFile();
            jTextFieldWSDLFile.setText(wsdlFile.getAbsolutePath());
            previousDirectory = wsdlFile.getPath();
        }
    }//GEN-LAST:event_jButtonWSDLFileBrowseActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowsePort;
    private javax.swing.JButton jButtonWSDLFileBrowse;
    private javax.swing.JLabel jLabelPort;
    private javax.swing.JLabel jLabelPortDescription;
    private javax.swing.JLabel jLabelWSDLFile;
    private javax.swing.JTextField jTextFieldPort;
    private javax.swing.JTextField jTextFieldWSDLFile;
    private javax.swing.JCheckBox useProviderBtn;
    // End of variables declaration//GEN-END:variables
    void validate(WizardDescriptor wizardDescriptor) {
    }

    private void initJsr109Info() {
        
        WSStackUtils wsStackUtils = new WSStackUtils(project);
        jsr109Supported = wsStackUtils.isJsr109Supported();
        jaxWsInJ2ee14Supported = ServerType.JBOSS == wsStackUtils.getServerType();
        noMetroInstalledOnGlassFishV3 = !jsr109Supported && ServerType.GLASSFISH_V3 == wsStackUtils.getServerType();
        jsr109oldSupported = wsStackUtils.isJsr109OldSupported();
        wm = WebModule.getWebModule(project.getProjectDirectory());
        wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
    }

    /**
     * If the project the web service is being created is not on a JSR 109 platform,
     * its Java source level must be at least 1.5
     */
    private boolean checkNonJsr109Valid() {
        if (wss != null) {
            if ((!jsr109Supported && !jsr109oldSupported) || jaxWsInJ2ee14Supported ||
                    (!jsr109Supported && jsr109oldSupported/* && jwsdpSupported*/)) {
                if (Util.isSourceLevel14orLower(project)) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_NeedProperSourceLevel")); // NOI18N
                    return false;
                }
            }
        }
        return true;
    }

    private boolean projectHasEmbeddedSpaces() {
        FileObject projectDir = project.getProjectDirectory();
        File projectDirFile = FileUtil.toFile(projectDir);
        String path = projectDirFile.getAbsolutePath();
        int index = path.indexOf(" ");
        return index != -1;
    }

    private boolean supportsJaxrpc() {
        boolean noJsr109InWeb = wm != null && !jsr109Supported && !jsr109oldSupported;
        boolean jaxWsInWeb14 = wm != null && jaxWsInJ2ee14Supported;

        return !Util.isJavaEE5orHigher(project) && !noJsr109InWeb && !jaxWsInWeb14;
    }

    boolean isValid(WizardDescriptor wizardDescriptor) {
        //first check for JDK compliance (for non-JSR 109)
        if (!checkNonJsr109Valid()) {
            return false;
        }
        if (supportsJaxrpc() && WebServicesSupport.getWebServicesSupport(project.getProjectDirectory()) == null) {
            // check if jaxrpc plugin installed
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_NoJaxrpcPluginFound")); // NOI18N
            return false;
        }

        String wsdlFilePath = jTextFieldWSDLFile.getText().trim();

        if (wsdlFilePath.length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, // NOI18N
                    NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_EnterWsdlName")); // NOI18N
            return false; // unspecified WSDL file
        }

        if (!wsdlFilePath.startsWith("http://") && !wsdlFilePath.startsWith("https://") && !wsdlFilePath.startsWith("www.")) { //NOI18N
            File f = new File(wsdlFilePath);
            f = getCanonicalFile(f);
            if (f == null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                        NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_WsdlInvalid")); // NOI18N
                return false; // invalid WSDL file
            }

            if (!f.exists()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                        NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_WsdlDoesNotExist")); // NOI18N
                return false; // invalid WSDL file
            }
        }

        if (Util.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project) || jaxWsInJ2ee14Supported ||
                (!jsr109Supported && !jsr109oldSupported ||
                (!jsr109Supported && jsr109oldSupported/* && jwsdpSupported*/))) {
            if (wsdlModel != null) {
                if (service == null) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_NoService")); // NOI18N
                    return false;
                }

                if (port == null) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_NoPort")); // NOI18N
                    return false;
                }
                
                if (findServiceInProject(service.getName())) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                            NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_ServiceNameExists", service.getName()));
                    return false; // Service name exists 
                }
            } else {
                if (wsdlServiceHandler != null && wsdlServiceHandler.getServiceName() != null && wsdlServiceHandler.getPortName() != null) {
                    if (findServiceInProject(wsdlServiceHandler.getServiceName())) {
                        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_ServiceNameExists", wsdlServiceHandler.getServiceName()));
                        return false; // Service name exists                        
                    } else {
                        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
                        return true;
                    }
                } else {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_NoPort")); // NOI18N
                    return false;
                }
            }
        }

        //warning if the project directory has embedded spaces
        //TODO - Remove this when the jwsdp version that fixes this problem is available
        if (projectHasEmbeddedSpaces()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_SPACE_IN_PROJECT_PATH")); // NOI18N
        } else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        }
        
        if (noMetroInstalledOnGlassFishV3) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_NoMetroInstalled")); //NOI18N            
        } else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
        }

        return true;
    }

    void store(WizardDescriptor d) {
        String wsdlLocation = jTextFieldWSDLFile.getText().trim();
        Boolean useProvider = this.useProviderBtn.isSelected();
        if (wsdlLocation.startsWith("www.")) {
            wsdlLocation = "http://" + wsdlLocation;
        } //NOI18N
        if (wsdlLocation.startsWith("http://") || wsdlLocation.startsWith("https://")) { //NOI18N
            d.putProperty(WizardProperties.WSDL_URL, wsdlLocation);
        } else {
            d.putProperty(WizardProperties.WSDL_FILE_PATH, wsdlLocation);
        }
        d.putProperty(WizardProperties.WSDL_MODEL, wsdlModel);
        d.putProperty(WizardProperties.WSDL_MODELER, wsdlModeler);
        d.putProperty(WizardProperties.WSDL_SERVICE, service);
        d.putProperty(WizardProperties.WSDL_PORT, port);
        d.putProperty(WizardProperties.WSDL_SERVICE_HANDLER, wsdlServiceHandler);
        d.putProperty(WizardProperties.USE_PROVIDER, useProvider);
    }

    void read(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WebServiceFromWSDLPanel.class);
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator<ChangeListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().stateChanged(e);
        }
    }

    private void updatePortBrowseButton() {
        wsdlModel = null;
        service = null;
        port = null;
        jTextFieldPort.setText(""); //NOI18N

        String wsdlFilePath = jTextFieldWSDLFile.getText().trim();

        if (wsdlFilePath.length() == 0) {
            jButtonBrowsePort.setEnabled(false);
        } else {
            if (!wsdlFilePath.startsWith("http://") && !wsdlFilePath.startsWith("https://") && !wsdlFilePath.startsWith("www.")) { //NOI18N
                File f = new File(wsdlFilePath);
                f = getCanonicalFile(f);
                if (f == null) {
                    jButtonBrowsePort.setEnabled(false);
                    return;
                } else if (!f.exists()) {
                    jButtonBrowsePort.setEnabled(false);
                    return;
                }
            }
            fireChange(); //call to disable Finish button
            if (Util.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project) || jaxWsInJ2ee14Supported ||
                    (!jsr109Supported && !jsr109oldSupported ||
                    (!jsr109Supported && jsr109oldSupported /*&& jwsdpSupported*/))) {
                createModel();
            }
        }
    }

    private void createModel() {
        String wsdlFilePath = jTextFieldWSDLFile.getText().trim();
        if (wsdlFilePath.startsWith("www.")) {
            wsdlFilePath = "http://" + wsdlFilePath;
        }
        if (wsdlFilePath.startsWith("http://") || wsdlFilePath.startsWith("https://")) {
            try {
                wsdlURL = new URL(wsdlFilePath);
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(jTextFieldWSDLFile.getText().trim()));
            wsdlURL = null;
            try {
                wsdlURL = normalizedWsdlFilePath.toURL();
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        generateWsdlModelTask.schedule(1000);
    }

    public void insertUpdate(DocumentEvent e) {
        updatePortBrowseButton();
        fireChange();
    }

    public void removeUpdate(DocumentEvent e) {
        updatePortBrowseButton();
        fireChange();
    }

    public void changedUpdate(DocumentEvent e) {
        updatePortBrowseButton();
        fireChange();
    }

    /** Retrieve the canonical version of a File instance, converting the possible
     *  IOException into a null (presumably for error presentation purposes).
     *  Copied from Utilities in core.
     *  FIX-ME: removet this when proper dependencies have been set up
     */
    public static File getCanonicalFile(File f) {
        File f1;
        try {
            f1 = f.getCanonicalFile();
        } catch (java.io.IOException e) {
            f1 = null;
        }
        return f1;
    }

    private static class WsdlFileFilter extends FileFilter {

        public boolean accept(File f) {
            String ext = FileUtil.getExtension(f.getName());
            return f.isDirectory() || "wsdl".equalsIgnoreCase(ext) || "asmx".equalsIgnoreCase(ext); // NOI18N
        }

        public String getDescription() {
            return NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_WsdlFilterDescription"); // NOI18N
        }
    }
    
    private boolean findServiceInProject(String serviceName) {
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        for (Object o:support.getServices()) {
            Service s = (Service)o;
            if (s.getWsdlUrl() != null && 
                    serviceName.equals(s.getServiceName())) {
                return true;
            }
        }
        return false;
    }
}
