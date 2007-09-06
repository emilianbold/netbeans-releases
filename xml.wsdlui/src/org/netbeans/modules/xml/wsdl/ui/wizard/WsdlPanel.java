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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.OperationPanel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Milan Kuchtiak
 */
final class WsdlPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String FILE_NAME = "FILE_NAME";
    
    public static final String WSDL_TARGETNAMESPACE = "WSDL_TARGETNAMESPACE";
    
    public static final String WSDL_DEFINITION_NAME = "WSDL_DEFINITION_NAME";
    
    
    private static final String DEFAULT_TARGET_NAMESPACE = "urn:WS/wsdl"; //NOI18N
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
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
/*            String errorMessage = null;
            //This should be good enough to disable html code.
            // If not try to use the StringEscapeUtils.escapeHtml from common lang.
            if (mErrorMessage != null) {
                errorMessage = "<html>" + Utility.escapeHtml(mErrorMessage) + "</html>";
            }*/
            
            templateWizard.putProperty ("WizardPanel_errorMessage", mErrorMessage); // NOI18N
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
        TemplateWizard wiz = (TemplateWizard) settings;
        
        if(templateWizard.getValue() == TemplateWizard.CANCEL_OPTION) {
            if (mTempWSDLModel != null) {
                DataObject dobj = ActionHelper.getDataObject(mTempWSDLModel);
                if (dobj != null) dobj.setModified(false);
            }
            return;
        }
        
        if (WizardDescriptor.PREVIOUS_OPTION.equals(((WizardDescriptor) settings).getValue())) {
            return;
        }
        
        
        String fileName = Templates.getTargetName(wiz);
        if (fileName == null) return;
        wiz.putProperty(FILE_NAME, fileName);
        String targetNamespace = getNS();
        wiz.putProperty(WSDL_TARGETNAMESPACE, targetNamespace);
        String definitionName = fileName;
        wiz.putProperty(WSDL_DEFINITION_NAME, definitionName);
        try {
            if (tempWSDLFile == null) {
                // Create a temporary file for storing our settings.
                tempWSDLFile = File.createTempFile(fileName + "RIT", ".wsdl"); // NOI18N
                tempWSDLFile.deleteOnExit();
                populateFileFromTemplate(tempWSDLFile);
                templateWizard.putProperty(
                        WizardPortTypeConfigurationStep.TEMP_WSDLFILE, tempWSDLFile);
                mTempWSDLModel = prepareModelFromFile(tempWSDLFile, definitionName);
                templateWizard.putProperty(
                        WizardPortTypeConfigurationStep.TEMP_WSDLMODEL, mTempWSDLModel);
            } else {
                templateWizard.putProperty(
                        WizardPortTypeConfigurationStep.TEMP_WSDLMODEL, mTempWSDLModel);
                templateWizard.putProperty(
                        WizardPortTypeConfigurationStep.TEMP_WSDLFILE, tempWSDLFile);
                mTempWSDLModel.startTransaction();
                mTempWSDLModel.getDefinitions().setTargetNamespace(targetNamespace);
                mTempWSDLModel.getDefinitions().setName(definitionName);
                if (mTempWSDLModel.getDefinitions().getTypes() == null) {
                    mTempWSDLModel.getDefinitions().setTypes(
                            mTempWSDLModel.getFactory().createTypes());
                }
                mTempWSDLModel.endTransaction();
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Create a temporary WSDL file that is based on the wizard template.
     *
     * @param  wizard  the template wizard.
     * @param  file    the WSDL file to be written.
     * @throws  DataObjectNotFoundException
     *          if the wizard template is missing.
     * @throws  IOException
     *          if unable to create the temporary file.
     */
    void populateFileFromTemplate(File file) throws
            DataObjectNotFoundException, IOException {
        if (templateWizard == null) {
            throw new IOException("templateWizard not defined");
        }
        FileObject template = Templates.getTemplate(templateWizard);
        DataObject dTemplate = DataObject.find(template);
        if (dTemplate != null) {
            EditorCookie editorCookie = DataObject.find(
                    dTemplate.getPrimaryFile()).getCookie(EditorCookie.class);
            editorCookie.openDocument();
            Document doc = editorCookie.getDocument();
            FileWriter writer = new FileWriter(file);
            try {
                writer.write(doc.getText(0, doc.getLength()));
            } catch (BadLocationException ble) {
                // This basically cannot happen.
                ErrorManager.getDefault().notify(ble);
            }
            writer.close();
        }
    }

    /**
     * Load and initialize the WSDL model from the given file, which should
     * already have a minimal WSDL definition. The preparation includes
     * setting the definition name, adding a namespace and prefix, and
     * adding the types component.
     *
     * @param  file  the file with a minimal WSDL definition.
     * @return  the model.
     */
    WSDLModel prepareModelFromFile(File file, String definitionName) {
        file = FileUtil.normalizeFile(file);
        FileObject fobj = FileUtil.toFileObject(file);
        ModelSource modelSource = org.netbeans.modules.xml.retriever.
                catalog.Utilities.getModelSource(fobj, fobj.canWrite());
        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);
        model.startTransaction();
        model.getDefinitions().setName(definitionName);
        String ns = getNS();
        model.getDefinitions().setTargetNamespace(ns);
        ((AbstractDocumentComponent) model.getDefinitions()).addPrefix("tns", ns);
        if (model.getDefinitions().getTypes() == null) {
            model.getDefinitions().setTypes(model.getFactory().createTypes());
        }
        model.endTransaction();
        return model;
    }

    String getNS() {
        String targetNamespace = gui.getNS();
        if (targetNamespace.length() == 0) {
            targetNamespace = DEFAULT_TARGET_NAMESPACE;
        }
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
                mErrorMessage = NbBundle.getMessage(OperationPanel.class, "ERR_MSG_INVALID_NAME" , text);
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
