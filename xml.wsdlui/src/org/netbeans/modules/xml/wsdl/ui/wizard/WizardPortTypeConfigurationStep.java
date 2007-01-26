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
 * WizardPortTypeConfigurationStep.java
 *
 * Created on August 31, 2006, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.ui.view.OperationType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.wsdl.ui.view.PortTypeConfigurationPanel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

/**
 *
 * @author radval
 */
public class WizardPortTypeConfigurationStep implements WizardDescriptor.FinishablePanel {
    
    public static final String PORTTYPE_NAME = "PORTTYPE_NAME";
    
    public static final String OPERATION_NAME = "OPERATION_NAME";
    
    public static final String OPERATION_TYPE = "OPERATION_TYPE";
   
    public static final String OPERATION_INPUT = "OPERATION_INPUT";
    
    public static final String OPERATION_OUTPUT = "OPERATION_OUTPUT";
    
    public static final String OPERATION_FAULT = "OPERATION_FAULT";
    
    public static final String OPERATION_INPUT_MESSAGE = "OPERATION_INPUT_MESSAGE";

    public static final String OPERATION_OUTPUT_MESSAGE = "OPERATION_OUTPUT_MESSAGE";
    
    public static final String OPERATION_FAULT_MESSAGE = "OPERATION_FAULT_MESSAGE";

    
    public static final String NAMESPACE_TO_PREFIX_MAP = "NAMESPACE_TO_PREFIX_MAP";
    
    public static final String TEMP_WSDLMODEL = "TEMP_WSDLMODEL";
    
    public static final String TEMP_WSDLFILE = "TEMP_WSDLFILE";
    
    public static final String PORTTYPE = "PORTTYPE";

    public static final String IS_FROM_WIZARD = "IS_FROM_WIZARD";
    
    
    private PortTypeConfigurationPanel mPanel;
    
    private Project project;
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private String mErrorMessage;
    
    private WizardDescriptor wiz = null;
    
    private WSDLModel mTempModel;
    
    private PortType mPortType;
    
    private List<Message> mNewMessageList = null;
    
    private ExtensibilityElement mPartnerLinkTypeElement = null;
    
    private Collection<Import> mImports = null;
    
    private File tempWSDLFile = null;
    
    /** Creates a new instance of WizardPortTypeConfigurationStep */
    public WizardPortTypeConfigurationStep(Project project) {
        this.project = project;

    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    public Component getComponent() {
        if (mPanel == null) {
            this.mPanel = new PortTypeConfigurationPanel(project);
//            this.mPanel.setPreferredSize(new Dimension(450, 400));
            this.mPanel.setName(NbBundle.getMessage(WizardPortTypeConfigurationStep.class, "LBL_WizardPortTypeConfigurationStep"));
            TextChangeListener listener  = new TextChangeListener();
            
            this.mPanel.getPortTypeNameTextField().getDocument().addDocumentListener(listener);
            this.mPanel.getOperationNameTextField().getDocument().addDocumentListener(listener);
        }
        return this.mPanel;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(WizardPortTypeConfigurationStep.class);
    }

    public boolean isValid() {
        wiz.putProperty ("WizardPanel_errorMessage", this.mErrorMessage); // NOI18N
        return this.mErrorMessage == null;
        
    }

    
    public void readSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard)settings;
        this.wiz = templateWizard;
        String fileName = (String) templateWizard.getProperty(WsdlPanel.FILE_NAME);
        if(this.mPanel.getPortTypeName() == null || this.mPanel.getPortTypeName().trim().equals("")) {
            this.mPanel.setPortTypeName(fileName + "PortType"); //NOI18N
        }
        
        if(this.mPanel.getOperationName() == null || this.mPanel.getOperationName().trim().equals("")) {
            this.mPanel.setOperationName(fileName + "Operation"); //NOI18N
        }
            
        
    }

    public void storeSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard)settings;
        if(templateWizard.getValue() == TemplateWizard.CANCEL_OPTION) {
        	return;
        }
        
        String portTypeName = this.mPanel.getPortTypeName();
        String operationName = this.mPanel.getOperationName();        
        OperationType ot = this.mPanel.getOperationType();
        
        templateWizard.putProperty(PORTTYPE_NAME, portTypeName);
        templateWizard.putProperty(OPERATION_NAME, operationName);
        templateWizard.putProperty(OPERATION_TYPE, ot);
        
        //operation input/output/fault
        List<PartAndElementOrTypeTableModel.PartAndElementOrType> inputMessageParts = this.mPanel.getInputMessageParts();
        List<PartAndElementOrTypeTableModel.PartAndElementOrType> outputMessageParts = this.mPanel.getOutputMessageParts();
        List<PartAndElementOrTypeTableModel.PartAndElementOrType> faultMessageParts = this.mPanel.getFaultMessageParts();

        templateWizard.putProperty(OPERATION_INPUT, inputMessageParts);
        templateWizard.putProperty(OPERATION_OUTPUT, outputMessageParts);
        templateWizard.putProperty(OPERATION_FAULT, faultMessageParts);
        Map<String, String> namespaceToPrefixMap = mPanel.getNamespaceToPrefixMap();
        templateWizard.putProperty(NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
        
        Map configurationMap = new HashMap();
        //portType
        configurationMap.put(WizardPortTypeConfigurationStep.PORTTYPE_NAME, portTypeName);
        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_NAME, operationName);
        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_TYPE, ot);

        //opertion type
        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_INPUT, inputMessageParts);
        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_OUTPUT, outputMessageParts);
        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_FAULT, faultMessageParts);
        configurationMap.put(WizardPortTypeConfigurationStep.NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
        configurationMap.put(WizardPortTypeConfigurationStep.IS_FROM_WIZARD, Boolean.TRUE);
        
        
//        try {
//        	String fileName = (String) templateWizard.getProperty(WsdlPanel.FILE_NAME);
//        	String targetNamespace = (String) templateWizard.getProperty(WsdlPanel.WSDL_TARGETNAMESPACE);
//        	String definitionName = (String) templateWizard.getProperty(WsdlPanel.WSDL_DEFINITION_NAME);
//        	
//        	if(tempWSDLFile == null) {
//            	tempWSDLFile = File.createTempFile(fileName + "TMP", ".wsdl"); //NOTI18N
//            	templateWizard.putProperty(TEMP_WSDLFILE, this.tempWSDLFile);
//            	
//            	FileWriter writer = new FileWriter(tempWSDLFile);
//                
//                StringBuffer emptyWsdl = new StringBuffer(100);
//                emptyWsdl.append("<definitions name=");
//                emptyWsdl.append("\"");
//                emptyWsdl.append(definitionName);
//                emptyWsdl.append("\" ");
//                emptyWsdl.append("targetNamespace=");
//                emptyWsdl.append("\"");
//                emptyWsdl.append(targetNamespace);
//                emptyWsdl.append("\" ");
//                emptyWsdl.append("xmlns:tns=");
//                emptyWsdl.append("\"");
//                emptyWsdl.append(targetNamespace);
//                emptyWsdl.append("\" ");
//                emptyWsdl.append("xmlns=\"http://schemas.xmlsoap.org/wsdl/\" ");
//                emptyWsdl.append("xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" ");
//                emptyWsdl.append("xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" ");
//                emptyWsdl.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"> ");
//                emptyWsdl.append("</definitions>");
//                
//                writer.write(emptyWsdl.toString());
//                
//                writer.close();
//                
//                FileObject tempWSDLFileObject = FileUtil.toFileObject(tempWSDLFile.getCanonicalFile());
//                ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(tempWSDLFileObject, 
//                tempWSDLFileObject.canWrite());
//                
//                
//                this.mTempModel = WSDLModelFactory.getDefault().getModel(modelSource);
//                
//        	}
//        	
            
            this.mTempModel = (WSDLModel) templateWizard.getProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL);
            if(this.mTempModel != null) {
                this.mTempModel.startTransaction();
                if(this.mPortType != null) {
                    this.mTempModel.getDefinitions().removePortType(this.mPortType);

                }

                if(this.mNewMessageList != null) {
                    Iterator<Message> it = mNewMessageList.iterator();
                    while(it.hasNext()) {
                        Message msg = it.next();
                        this.mTempModel.getDefinitions().removeMessage(msg);
                    }
                }

                if(this.mPartnerLinkTypeElement != null) {
                    this.mTempModel.getDefinitions().removeExtensibilityElement(this.mPartnerLinkTypeElement);
                }

                if(this.mImports != null) {
                    Collection<Schema> schemas = this.mTempModel.getDefinitions().getTypes().getSchemas();
                    if(schemas.size() != 0) {
                        Schema schema = schemas.iterator().next();
                        Iterator<Import> it = this.mImports.iterator();
                        while(it.hasNext()) {
                            Import imp = it.next();
                            schema.removeExternalReference(imp);
                        }
                    }
                }
                
                PortTypeGenerator ptGen = new PortTypeGenerator(this.mTempModel, configurationMap);
                ptGen.execute();
                this.mPortType = ptGen.getPortType();
                this.mNewMessageList = ptGen.getNewMessages();
                this.mPartnerLinkTypeElement = ptGen.getPartnerLinkType();
                this.mImports = ptGen.getImports();
                
                this.mTempModel.endTransaction();

                templateWizard.putProperty(PORTTYPE, this.mPortType);
//            templateWizard.putProperty(TEMP_WSDLMODEL, this.mTempModel);
            }
//        } catch(IOException ex) {
//            
//        }
        
        
    }
    
    
    private boolean isValidName(Document doc) {
        try {
            String text = doc.getText(0, doc.getLength());
            boolean isValid  = org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(text);
            if(!isValid) {
                mErrorMessage = "Name \"" + text + "\" is not a valid NCName";
            } else {
                mErrorMessage = null;
            }
            
        }  catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return mErrorMessage == null;
    }
    
    private void validate() {
        boolean validPortType = isValidName(this.mPanel.getPortTypeNameTextField().getDocument());
        if(!validPortType) {
            fireChangeEvent();
            return;
        }
        
        boolean validOperation = isValidName(this.mPanel.getOperationNameTextField().getDocument());
        
        if(!validOperation) {
            fireChangeEvent();
            return;
        }
        
        fireChangeEvent();
    }
    
    private void fireChangeEvent() {
        Iterator<ChangeListener> it = this.listeners.iterator();
        ChangeEvent e = new ChangeEvent(this);
        while(it.hasNext()) {
            ChangeListener l = it.next();
            l.stateChanged(e);
        }
    }

    public boolean isFinishPanel() {
        return true;
    }
    class TextChangeListener implements DocumentListener {
     
         public void changedUpdate(DocumentEvent e) {
            validate();
         }
         
         public void insertUpdate(DocumentEvent e) {
             validate();
         }

         public void removeUpdate(DocumentEvent e) {
             validate();
         }
 
    }
}
