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
import java.util.Map;
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
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
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
    private boolean jwsdpSupported;
    private boolean wsitSupported;
    
    private RequestProcessor.Task generateWsdlModelTask;
    private URL wsdlURL;
    
    /** Creates new form WebServiceFromWSDLPanel */
    public WebServiceFromWSDLPanel(Project project) {
        this.project = project;
        initComponents();
        initJsr109Info();
        
        jTextFieldWSDLFile.getDocument().addDocumentListener(this);
        
        generateWsdlModelTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                
                WsdlWrapperHandler handler = null;
                try {
                    handler=WsdlWrapperGenerator.parse(wsdlURL.toExternalForm());
                } catch (ParserConfigurationException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                } catch (SAXException ex) {
                    String mes = NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_SAXException", ex.getLocalizedMessage()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                }
                if (handler!=null && !handler.isServiceElement()) {
                    StreamSource source = new StreamSource(wsdlURL.toExternalForm());
                    try {
                        File wsdlFile = new File(System.getProperty("java.io.tmpdir"), WsdlWrapperGenerator.getWrapperName(wsdlURL)); //NOI18N

                        if(!wsdlFile.exists()) {
                            try {
                                wsdlFile.createNewFile();
                            } catch(IOException ex) {
                                String mes = NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(desc);
                                return;
                            }
                        }
                        WsdlWrapperGenerator.generateWrapperWSDLContent(wsdlFile,source,handler.getTargetNsPrefix(),wsdlURL.toExternalForm());
                        wsdlURL=wsdlFile.toURL();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    }
                }
                
                final WsdlWrapperHandler wsdlHandler = handler;
                
                wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlURL);
                wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                    public void modelCreated(WsdlModel model) {
                        wsdlModel=model;
                        if (wsdlModel==null) {
                            String serviceName=null;
                            String portName=null;
                            try {
                                wsdlServiceHandler = WsdlServiceHandler.parse(wsdlURL.toExternalForm());
                                serviceName = wsdlServiceHandler.getServiceName();
                                portName = wsdlServiceHandler.getPortName();
                            } catch (ParserConfigurationException ex) {
                            } catch (SAXException ex) {
                            } catch (IOException ex) {} 
                            if (serviceName!=null && portName!=null) {
                                jTextFieldPort.setText(serviceName + "#" + portName);
                            } else {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                        NbBundle.getMessage(WebServiceFromWSDLPanel.class,"TXT_CannotGenerateService",
                                                            wsdlModeler.getCreationException().getLocalizedMessage()),
                                        NotifyDescriptor.WARNING_MESSAGE)
                                );
                            }
                        } else {
                            jButtonBrowsePort.setEnabled(true);
                            List services = wsdlModel.getServices();
                            if (services != null && services.size() > 0) {
                                service = (WsdlService) services.get(0);
                                List ports = service.getPorts();
                                if (ports != null && ports.size() > 0)
                                    port = (WsdlPort) ports.get(0);
                            }
                        }

                        if (service != null && port != null) {
                            jTextFieldPort.setText(service.getName() + "#" + port.getName()); //NOI18N
                            if (wsdlHandler!=null) {
                                String bindingType = wsdlHandler.getBindingTypeForPort(port.getName());
                                if (bindingType!=null) port.setSOAPVersion(bindingType);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabelWSDLFile = new javax.swing.JLabel();
        jTextFieldWSDLFile = new javax.swing.JTextField();
        jButtonWSDLFileBrowse = new javax.swing.JButton();
        jLabelPortDescription = new javax.swing.JLabel();
        jLabelPort = new javax.swing.JLabel();
        jTextFieldPort = new javax.swing.JTextField();
        jButtonBrowsePort = new javax.swing.JButton();

        jLabelWSDLFile.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("MNE_WSDL_File").charAt(0));
        jLabelWSDLFile.setLabelFor(jTextFieldWSDLFile);
        jLabelWSDLFile.setText(org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_WSDL_File"));

        jTextFieldWSDLFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("A11Y_WSDLFilePath"));

        jButtonWSDLFileBrowse.setMnemonic(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("MNE_Browse").charAt(0));
        jButtonWSDLFileBrowse.setText(org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_Browse"));
        jButtonWSDLFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWSDLFileBrowseActionPerformed(evt);
            }
        });

        jButtonWSDLFileBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("A11Y_WSDLFileButton"));

        jLabelPortDescription.setText(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("LBL_PortMessage"));

        jLabelPort.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("MNE_WebServicePort").charAt(0));
        jLabelPort.setLabelFor(jTextFieldPort);
        jLabelPort.setText(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("LBL_WebServicePort"));

        jTextFieldPort.setEditable(false);
        jTextFieldPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("A11Y"));

        jButtonBrowsePort.setMnemonic(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("MNE_BrowsePort").charAt(0));
        jButtonBrowsePort.setText(org.openide.util.NbBundle.getBundle(WebServiceFromWSDLPanel.class).getString("LBL_Browse"));
        jButtonBrowsePort.setEnabled(false);
        jButtonBrowsePort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowsePortActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelPort)
                    .add(jLabelWSDLFile))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
                    .add(jTextFieldWSDLFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jButtonBrowsePort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonWSDLFileBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .add(jLabelPortDescription)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelWSDLFile)
                    .add(jButtonWSDLFileBrowse)
                    .add(jTextFieldWSDLFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelPortDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelPort)
                    .add(jButtonBrowsePort)
                    .add(jTextFieldPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(223, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButtonBrowsePortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowsePortActionPerformed
        Node root = new WsdlNode(wsdlModel);
        PortChooser chooser = new PortChooser(root);
        final DialogDescriptor dd = new DialogDescriptor(chooser, org.openide.util.NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_SelectPortDescription")); //NOI18N

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
            if (Util.isJavaEE5orHigher(project) ||
                    (!jsr109Supported && !jsr109oldSupported ||
                    (!jsr109Supported && jsr109oldSupported && jwsdpSupported )) || 
                    (wsitSupported)) {
                jTextFieldPort.setText(chooser.getSelectedPortOwnerName() + "#" + chooser.getSelectedNodes()[0].getDisplayName()); //NOI18N
                service = wsdlModel.getServiceByName(chooser.getSelectedPortOwnerName());
                port = service.getPortByName(chooser.getSelectedNodes()[0].getDisplayName());
            }
        }
    }//GEN-LAST:event_jButtonBrowsePortActionPerformed
    
    private void jButtonWSDLFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWSDLFileBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(previousDirectory);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(WSDL_FILE_FILTER);
        chooser.setFileFilter(WSDL_FILE_FILTER);
        
        if(chooser.showOpenDialog(WebServiceFromWSDLPanel.this) == JFileChooser.APPROVE_OPTION) {
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
    // End of variables declaration//GEN-END:variables
    
    void validate(WizardDescriptor wizardDescriptor) {
    }

    private void initJsr109Info() {
        wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    jsr109Supported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
                    jsr109oldSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
                    jwsdpSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
                    wsitSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT);
                }
            }
        }
    }
    
    /**
     * If the project the web service is being created is not on a JSR 109 platform,
     * its Java source level must be at least 1.5
     */
    private boolean checkNonJsr109Valid() {
        if (wss != null) {
            if(!jsr109Supported && !jsr109oldSupported ||
                    (!jsr109Supported && jsr109oldSupported && jwsdpSupported )){
                if (Util.isSourceLevel14orLower(project)) {
                    wizardDescriptor.putProperty("WizardPanel_errorMessage",
                            NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_NeedProperSourceLevel")); // NOI18N
                    return false;
                }
            }
        }                    
        return true;
    }
    
    private boolean projectHasEmbeddedSpaces(){
        FileObject projectDir = project.getProjectDirectory();
        File projectDirFile = FileUtil.toFile(projectDir);
        String path = projectDirFile.getAbsolutePath();
        int index = path.indexOf(" ");
        return index != -1;
    }
    
    boolean isValid(WizardDescriptor wizardDescriptor) {
        //first check for JDK compliance (for non-JSR 109)
        if(!checkNonJsr109Valid()){
            return false;
        }
        
        String wsdlFilePath = jTextFieldWSDLFile.getText().trim();
        
        if(wsdlFilePath == null || wsdlFilePath.length() == 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",  // NOI18N
                    NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_EnterWsdlName")); // NOI18N
            return false; // unspecified WSDL file
        }
        
        File f = new File(wsdlFilePath);
        String wsdlFileText = f.getAbsolutePath();
        f = getCanonicalFile(f);
        if(f == null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",  // NOI18N
                    NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_WsdlInvalid")); // NOI18N
            return false; // invalid WSDL file
        }
        
        if(!f.exists()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",  // NOI18N
                    NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_WsdlDoesNotExist")); // NOI18N
            return false; // invalid WSDL file
        }
        
        if (Util.isJavaEE5orHigher(project) ||
                (!jsr109Supported && !jsr109oldSupported ||
                (!jsr109Supported && jsr109oldSupported && jwsdpSupported )) ||
                (wsitSupported)) {
            if (wsdlModel != null) {
                if (service == null) {
                    wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_NoService")); // NOI18N
                    return false;
                }

                if (port == null) {
                    wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_NoPort")); // NOI18N
                    return false;
                }
            } else {
                if (wsdlServiceHandler!=null && wsdlServiceHandler.getServiceName()!=null && wsdlServiceHandler.getPortName()!=null) {
                    return true;
                } else {
                    wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_NoPort")); // NOI18N
                    return false;
                }
            }
        }
        
        //warning if the project directory has embedded spaces
        //TODO - Remove this when the jwsdp version that fixes this problem is available
        if(projectHasEmbeddedSpaces()){
            wizardDescriptor.putProperty("WizardPanel_errorMessage", 
                    NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_SPACE_IN_PROJECT_PATH")); // NOI18N
        }
        else{
            wizardDescriptor.putProperty("WizardPanel_errorMessage",  ""); // NOI18N
        }
// Retouche      
//        if (JavaMetamodel.getManager().isScanInProgress()) {
//            if (!isWaitingForScan) {
//                isWaitingForScan = true;
//                RequestProcessor.getDefault().post(new Runnable() {
//                    public void run() {
//                        JavaMetamodel.getManager().waitScanFinished();
//                        isWaitingForScan = false;
//                        fireChange();
//                    }
//                });
//            }
//            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(WebServiceFromWSDLPanel.class, "MSG_ScanningInProgress")); //NOI18N
//            return false;
//        } else
            wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); //NOI18N

        return true;
    }
    
    void store(WizardDescriptor d) {
        d.putProperty(WizardProperties.WSDL_FILE_PATH, jTextFieldWSDLFile.getText().trim());
        d.putProperty(WizardProperties.WSDL_MODEL, wsdlModel);
        d.putProperty(WizardProperties.WSDL_MODELER, wsdlModeler);
        d.putProperty(WizardProperties.WSDL_SERVICE, service);
        d.putProperty(WizardProperties.WSDL_PORT, port);
        d.putProperty(WizardProperties.WSDL_SERVICE_HANDLER,wsdlServiceHandler);
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
        
        if(wsdlFilePath == null || wsdlFilePath.length() == 0) {
            jButtonBrowsePort.setEnabled(false);
        } else {
            File f = new File(wsdlFilePath);
            String wsdlFileText = f.getAbsolutePath();
            f = getCanonicalFile(f);
            if(f == null) {
                jButtonBrowsePort.setEnabled(false);
            } else if(!f.exists()) {
                jButtonBrowsePort.setEnabled(false);
            } else {
                fireChange(); //call to disable Finish button
                if (Util.isJavaEE5orHigher(project) ||
                        (!jsr109Supported && !jsr109oldSupported ||
                        (!jsr109Supported && jsr109oldSupported && jwsdpSupported)) ||
                        (wsitSupported)) {
                    createModel();
                }
            }
        }
    }
    
    private void createModel() {
        File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(jTextFieldWSDLFile.getText().trim()));
        wsdlURL = null;
        try {
            wsdlURL = normalizedWsdlFilePath.toURL();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
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
            boolean result;
            if(f.isDirectory() || "wsdl".equalsIgnoreCase(FileUtil.getExtension(f.getName()))) { // NOI18N
                result = true;
            } else {
                result = false;
            }
            return result;
        }
        
        public String getDescription() {
            return NbBundle.getMessage(WebServiceFromWSDLPanel.class, "LBL_WsdlFilterDescription"); // NOI18N
        }
    }

}
