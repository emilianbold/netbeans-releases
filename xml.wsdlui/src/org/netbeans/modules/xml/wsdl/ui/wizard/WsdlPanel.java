/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.OperationPanel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Milan Kuchtiak
 */
public final class WsdlPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String FILE_NAME = "FILE_NAME";
    
    public static final String WSDL_TARGETNAMESPACE = "WSDL_TARGETNAMESPACE";
    
    public static final String WSDL_DEFINITION_NAME = "WSDL_DEFINITION_NAME";
    public static final String ENCODING = "PROJECT_ENCODING";
    public static final String GENERATE_PARTNER_LINKTYPE = 
            "GENERATE_PARTNER_LINKTYPE";
    
    private static final String DEFAULT_TARGET_NAMESPACE = "urn:WS/wsdl"; //NOI18N
    
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private WsdlUIPanel gui;

    private Project project;
    private TemplateWizard templateWizard;
    
    private JTextField fileNameTextField;
    
    private File tempWSDLFile = null;
    
    private WSDLModel mTempWSDLModel = null;
    
    private String mErrorMessage;
    
    private TextChangeListener mListener = new TextChangeListener();
    
    private boolean hasNext = true;
    private boolean isFinishable = true;
    private WSDLWizardContext context;
    private boolean mGeneratePartnerLinkType = true;
    
    public WsdlPanel(WSDLWizardContext context, Project project) {
        this.context = context;
        this.project = project;
    }
    
    public WSDLWizardContext getWSDLWizardContext() {
        return context;
    }
    
    public TemplateWizard getTemplateWizard() {
        return templateWizard;
    }
    
    public void setNameTF(JTextField nameTF) {
        gui.attachFileNameListener(nameTF);
        if(nameTF != null) {
            nameTF.getDocument().removeDocumentListener(mListener);
            nameTF.getDocument().addDocumentListener(mListener);
            fileNameTextField = nameTF;
        }
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new WsdlUIPanel(this);
            gui.getSchemaFileTextField().addPropertyChangeListener(new SchemaImportTextChangeListener());
            gui.getSchemaFileTextField().getDocument().addDocumentListener(new SchemaImportTextChangeListener());
            gui.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("HAS_NEXT")) {
                        hasNext = ((Boolean)evt.getNewValue()).booleanValue();
                        context.setHasNext(hasNext);
                    } else if (evt.getPropertyName().equals("IS_FINISHABLE")) {
                        isFinishable = ((Boolean)evt.getNewValue()).booleanValue();
                    }
                    changeSupport.fireChange();
                }
            });
        }
        return gui;
    }
 
    public Project getProject(){
        return project;
    }    
    
    public HelpCtx getHelp() {
        return new HelpCtx(WsdlPanel.class);
    }

    public void cleanup() {
        WSDLModel tempModel = (WSDLModel) templateWizard.getProperty(WSDLWizardConstants.TEMP_WSDLMODEL);
        if (tempModel != null) {
            DataObject dobj = ActionHelper.getDataObject(tempModel);
            if (dobj != null) {
                dobj.setModified(false);
                try {
                    dobj.delete();
                } catch (Exception e) {
                    //ignore
                }
            }
        }
        templateWizard.putProperty(WSDLWizardConstants.TEMP_WSDLMODEL, null);

        if (mTempWSDLModel != null) {
            DataObject dobj = ActionHelper.getDataObject(mTempWSDLModel);
            if (dobj != null) {
                dobj.setModified(false);
                try {
                    dobj.delete();
                } catch (Exception e) {
                    //ignore
                }
            }
            mTempWSDLModel = null;
        }
        templateWizard.putProperty(WSDLWizardConstants.TEMP_WSDLFILE, null);
        tempWSDLFile = null;
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
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public void readSettings( Object settings ) {
        templateWizard = (TemplateWizard)settings;
         
        // when WsdlUIPanel was instantiated, the templateWizard (which
        // has the flag to enable the concrete configuration only) is still null so need to query here
        if (gui != null) {
            boolean disableAbstract = false;
            Object flag = templateWizard.getProperty("bindingConcreteConfiguration");
            if ((templateWizard != null) && (flag != null) &&
                    (flag instanceof Boolean)) {
                Boolean boolVal = Boolean.valueOf(flag.toString());
                if (boolVal.booleanValue()) {
                    disableAbstract = true;
                }           
           
            }            
            gui.disableWSDLTypeSection(disableAbstract);
        }
        gui.updateNS();
    }

    public void storeSettings(Object settings) {
        TemplateWizard wiz = (TemplateWizard) settings;
        Object option = wiz.getValue();
        if(option == NotifyDescriptor.CANCEL_OPTION || option == WizardDescriptor.PREVIOUS_OPTION) {
            cleanup();
            return;
        }
        
        String fileName = Templates.getTargetName(wiz);
        if (fileName == null) return;
        wiz.putProperty(FILE_NAME, fileName);
        String targetNamespace = getNS();
        wiz.putProperty(WSDL_TARGETNAMESPACE, targetNamespace);
        String definitionName = fileName;
        wiz.putProperty(WSDL_DEFINITION_NAME, definitionName);
        wiz.putProperty(GENERATE_PARTNER_LINKTYPE, mGeneratePartnerLinkType);
        try {
            //LocalizedTemplateGroup ltg = (LocalizedTemplateGroup) templateWizard.getProperty(WSDLWizardConstants.BINDING_TYPE);
            LocalizedTemplate lt  = (LocalizedTemplate) templateWizard.getProperty(WSDLWizardConstants.BINDING_SUBTYPE);

            // fix for issue #160855 - NPE at org.netbeans.modules.xml.xdm.XDMModel.flushDocument
            Object isNewTmpFileRequest  = wiz.getProperty(
                WSDLWizardConstants.CREATE_NEW_TEMP_WSDLFILE);
            if ((isNewTmpFileRequest instanceof Boolean) && ((Boolean) isNewTmpFileRequest)) {
                tempWSDLFile = null;
            }
            
            if (tempWSDLFile != null && gui != null) {
                LocalizedTemplate userSelectedBindingSubType = gui.getBindingSubType();
                if (lt != null) {
                    //If lt is not null, then it means concrete was selected before.
                    //So cleanup the model unless the same binding is selected.
                    if (userSelectedBindingSubType == null || !lt.getName().equals(userSelectedBindingSubType.getName())) {
                        cleanup();
                    }
                } else {
                    if (userSelectedBindingSubType != null) {
                        cleanup();
                    }
                }
            }
            
            if (tempWSDLFile == null) {
                
                // Create a temporary file for storing our settings.
                tempWSDLFile = File.createTempFile(fileName + "RIT", ".wsdl"); // NOI18N
                if (gui != null && gui.getWSDLTemplateStream() != null) {
                    InputStream stream = gui.getWSDLTemplateStream();
                    String encoding = (String) templateWizard.getProperty(ENCODING);
                    if (stream.markSupported()) {
                        stream.reset();
                    }
                    BufferedReader reader = null;
                    StringBuilder strBuilder = new StringBuilder();
                    try {
                        reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            strBuilder.append(line).append('\n');
                        }
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                    
                    String content = strBuilder.toString();
                    content = content.replaceAll("#SERVICE_NAME", fileName);
                    content = content.replaceAll("#TARGET_NAMESPACE", targetNamespace);
                    
                    FileOutputStream outputFileStream = null;
                    OutputStreamWriter writer = null;
                    try {
                        outputFileStream = new FileOutputStream(tempWSDLFile);
                        writer = new OutputStreamWriter(outputFileStream, encoding);
                        writer.write(content);
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                        if (outputFileStream != null) {
                            outputFileStream.close();
                        }
                    }
                    
                } else {
                    populateFileFromTemplate(tempWSDLFile);
                }

                tempWSDLFile.deleteOnExit();
                templateWizard.putProperty(
                        WSDLWizardConstants.TEMP_WSDLFILE, tempWSDLFile);

                // fix for issue #160855 - NPE at org.netbeans.modules.xml.xdm.XDMModel.flushDocument
                wiz.putProperty(WSDLWizardConstants.CREATE_NEW_TEMP_WSDLFILE, null);

                mTempWSDLModel = prepareModelFromFile(tempWSDLFile, definitionName);
                wiz.putProperty(
                        WSDLWizardConstants.TEMP_WSDLMODEL, mTempWSDLModel);
            } else {
                wiz.putProperty(
                        WSDLWizardConstants.TEMP_WSDLMODEL, mTempWSDLModel);
                wiz.putProperty(
                        WSDLWizardConstants.TEMP_WSDLFILE, tempWSDLFile);
                mTempWSDLModel.startTransaction();
                mTempWSDLModel.getDefinitions().setTargetNamespace(targetNamespace);
                mTempWSDLModel.getDefinitions().setName(definitionName);
                if (mTempWSDLModel.getDefinitions().getTypes() == null) {
                    mTempWSDLModel.getDefinitions().setTypes(
                            mTempWSDLModel.getFactory().createTypes());
                }
                mTempWSDLModel.endTransaction();
            }
            
            ((WSDLWizardContextImpl)context).setWSDLModel(mTempWSDLModel);
            
            if (gui != null) {
                templateWizard.putProperty(WSDLWizardConstants.BINDING_TYPE, gui.getBindingType());
                templateWizard.putProperty(WSDLWizardConstants.BINDING_SUBTYPE, gui.getBindingSubType());
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
        
        String encoding = (String) templateWizard.getProperty(ENCODING);
        
        FileObject template = Templates.getTemplate(templateWizard);
        DataObject dTemplate = DataObject.find(template);
        if (dTemplate != null) {
            EditorCookie editorCookie = DataObject.find(
                    dTemplate.getPrimaryFile()).getCookie(EditorCookie.class);
            editorCookie.openDocument();
            Document doc = editorCookie.getDocument();
            StringBuilder stringBuilder = new StringBuilder();
            try {
                stringBuilder.append(doc.getText(0, doc.getLength()));
            } catch (BadLocationException ble) {
                // This basically cannot happen.
                ErrorManager.getDefault().notify(ble);
            }
            if (!encoding.equalsIgnoreCase("UTF-8")) {
                stringBuilder.delete(30, 35);
                stringBuilder.insert(30, encoding);
            }
            String content = stringBuilder.toString();

            FileOutputStream stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream, encoding);
            writer.write(content);
            writer.close();
            stream.close();
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
        File f = FileUtil.normalizeFile(file);
        FileObject fobj = FileUtil.toFileObject(f);
        ModelSource modelSource = org.netbeans.modules.xml.retriever.
                catalog.Utilities.getModelSource(fobj, fobj.canWrite());
        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);
        if (model.getState() == WSDLModel.State.VALID) {
            model.startTransaction();
            model.getDefinitions().setName(definitionName);
            String ns = getNS();
            model.getDefinitions().setTargetNamespace(ns);
            ((AbstractDocumentComponent) model.getDefinitions()).addPrefix("tns", ns);
            if (model.getDefinitions().getTypes() == null) {
                model.getDefinitions().setTypes(model.getFactory().createTypes());
            }
            model.endTransaction();
        } else {
            assert false : "Model is invalid, correct the template if any";
        }
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
        return isFinishable;
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
            changeSupport.fireChange();
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
            changeSupport.fireChange();
            return;
        }
        mErrorMessage = null;
        changeSupport.fireChange();
    }
    
    
    public boolean hasNext() {
        return hasNext;
    }
    
}
