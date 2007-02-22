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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Milan Kuchtiak
 */
final class WsdlPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String FILE_NAME = "FILE_NAME";
    
    public static final String WSDL_TARGETNAMESPACE = "WSDL_TARGETNAMESPACE";
    
    public static final String WSDL_DEFINITION_NAME = "WSDL_DEFINITION_NAME";
    
    
    private static final String DEFAULT_TARGET_NAMESPACE = "urn:WS/wsdl"; //NOI18N
    
    private final List<ChangeListener> listeners = new ArrayList();
    private WsdlUIPanel gui;

    private Project project;
    private TemplateWizard templateWizard;
    
    private JTextField fileNameTextField;
    
    private File tempWSDLFile = null;
    
    private WSDLModel mTempWSDLModel = null;
    
    private String mErrorMessage;
    
    private TextChangeListener mListener = new TextChangeListener();
    
    WsdlPanel(Project project, SourceGroup[] folders) {
        this.project = project;
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    }
    
    void setNameTF(JTextField nameTF) {
        gui.attachFileNameListener(nameTF);
        if(nameTF != null) {
            nameTF.getDocument().removeDocumentListener(mListener);//remove existing one
            nameTF.getDocument().addDocumentListener(mListener);
            fileNameTextField = nameTF;
        }
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new WsdlUIPanel(this);
            gui.getSchemaFileTextField().addPropertyChangeListener(new SchemaImportTextChangeListener());
            gui.getSchemaFileTextField().getDocument().addDocumentListener(new SchemaImportTextChangeListener());
//            gui.setPreferredSize(new Dimension(450, 400));
        }
        return gui;
    }
 
    public Project getProject(){
        return project;
    }    
    
    public HelpCtx getHelp() {
        return new HelpCtx(WsdlPanel.class);
    }

    
    
    public boolean isValid() {
        if(templateWizard != null) {
            templateWizard.putProperty ("WizardPanel_errorMessage", this.mErrorMessage); // NOI18N
        }
        return this.mErrorMessage == null;
        
    }
    
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    protected void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( Object settings ) {
        templateWizard = (TemplateWizard)settings;
        
        
        //if user come to first panel we need to discard out temp wsdl model
        templateWizard.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL, null);
        templateWizard.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLFILE, null);
        
    }

    public void storeSettings(Object settings) {
        TemplateWizard wiz = (TemplateWizard)settings;
        
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        
//        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
        String fileName = Templates.getTargetName(wiz);
        ((WizardDescriptor)settings).putProperty (FILE_NAME, fileName); // NOI18N
        String targetNamespace = getNS();
        ((WizardDescriptor)settings).putProperty (WSDL_TARGETNAMESPACE, targetNamespace); // NOI18N
        
        String definitionName = fileName;
        ((WizardDescriptor)settings).putProperty (WSDL_DEFINITION_NAME, definitionName); // NOI18N
        
        
        FileObject template = Templates.getTemplate( wiz );
        
        try {
            DataObject dTemplate = DataObject.find( template );
            if(tempWSDLFile == null) {
                if(dTemplate != null) {
                    //create a temp file
                	tempWSDLFile = File.createTempFile(fileName + "RIT", ".wsdl"); //NOTI18N
                    tempWSDLFile.deleteOnExit();
                    templateWizard.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLFILE, this.tempWSDLFile);
                    
                    FileObject templateFileObject = dTemplate.getPrimaryFile();
                    
                    //write content from template to tmp file
                    DataObject templateDobj = DataObject.find(templateFileObject);
                    EditCookie edit = (EditCookie) templateDobj.getCookie(EditCookie.class);
                    EditorCookie editorCookie = (EditorCookie)templateDobj.getCookie(EditorCookie.class);
                    editorCookie.openDocument();
                    javax.swing.text.Document doc = editorCookie.getDocument();

                    //write from tempModel to actual file
                    FileWriter writer = new FileWriter(tempWSDLFile);
                    try {
                        writer.write(doc.getText(0, doc.getLength()));
                        writer.close();
                    } catch(Exception ex) {
                            ErrorManager.getDefault().notify(ex);
                    }
                    
                    writer.close();
                            
                    FileObject tempWSDLFileObject = FileUtil.toFileObject(tempWSDLFile.getCanonicalFile());
                    ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(tempWSDLFileObject, 
                    tempWSDLFileObject.canWrite());
                    
                    this.mTempWSDLModel  = WSDLModelFactory.getDefault().getModel(modelSource);
                    this.mTempWSDLModel.startTransaction();
                    this.mTempWSDLModel.getDefinitions().setName(definitionName);
                    this.mTempWSDLModel.getDefinitions().setTargetNamespace(targetNamespace);
                    ((AbstractDocumentComponent) this.mTempWSDLModel.getDefinitions()).addPrefix("tns", targetNamespace);
                    if (mTempWSDLModel.getDefinitions().getTypes() == null) {
                        mTempWSDLModel.getDefinitions().setTypes(mTempWSDLModel.getFactory().createTypes());
                    }
                    this.mTempWSDLModel.endTransaction();
                    
                    templateWizard.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL, this.mTempWSDLModel);
                }
            } else {
            	templateWizard.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL, this.mTempWSDLModel);
                templateWizard.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLFILE, tempWSDLFile);
                
                this.mTempWSDLModel.startTransaction();
                this.mTempWSDLModel.getDefinitions().setTargetNamespace(targetNamespace);
                this.mTempWSDLModel.getDefinitions().setName(definitionName);
                if (mTempWSDLModel.getDefinitions().getTypes() == null) {
                    mTempWSDLModel.getDefinitions().setTypes(mTempWSDLModel.getFactory().createTypes());
                }
                this.mTempWSDLModel.endTransaction();
            }
                
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
    }
    
    String getNS() {
        String targetNamespace = gui.getNS();
        if (targetNamespace.length()==0) targetNamespace = DEFAULT_TARGET_NAMESPACE;
        return targetNamespace;
    }
    
    WsdlUIPanel.SchemaInfo[] getSchemas() {
        return gui.getSchemas();
    }
    
    boolean isImport() {
        return gui.isImport();
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    private void fireChangeEvent() {
        Iterator<ChangeListener> it = this.listeners.iterator();
        ChangeEvent e = new ChangeEvent(this);
        while(it.hasNext()) {
            ChangeListener l = it.next();
            l.stateChanged(e);
        }
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
    
    
    
    private void validateFileName() {
        boolean validFileName = isValidName(this.fileNameTextField.getDocument());
        if(!validFileName) {
            fireChangeEvent();
            return;
        }
    }

    
    class TextChangeListener implements DocumentListener {
         
         public void changedUpdate(DocumentEvent e) {
            validateFileName();
         }
         
         public void insertUpdate(DocumentEvent e) {
             validateFileName();
         }

         public void removeUpdate(DocumentEvent e) {
             validateFileName();
         }
 
    }
    
    class SchemaImportTextChangeListener implements PropertyChangeListener, DocumentListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("VALUE_SET")) {
                validateSchemas();
            }
        }

        public void insertUpdate(DocumentEvent e) {
            validateSchemas();
        }

        public void removeUpdate(DocumentEvent e) {
            validateSchemas();
        }

        public void changedUpdate(DocumentEvent e) {
            validateSchemas();
        }

   }
    
    public void validateSchemas() {
        try {
            gui.validateSchemas();
        } catch (WizardValidationException e) {
            mErrorMessage = e.getLocalizedMessage();
            fireChangeEvent();
            return;
        }
        mErrorMessage = null;
        fireChangeEvent();
    }
    
    
    
    
}
