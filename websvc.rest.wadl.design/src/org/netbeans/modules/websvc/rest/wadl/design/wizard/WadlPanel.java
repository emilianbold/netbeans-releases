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

package org.netbeans.modules.websvc.rest.wadl.design.wizard;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Ayub Khan
 */
final class WadlPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String FILE_NAME = "FILE_NAME";
    
    public static final String WADL_SERVICEURL = "WADL_SERVICEURL";
    
    public static final String WADL_DEFINITION_NAME = "WADL_DEFINITION_NAME";
    public static final String ENCODING = "PROJECT_ENCODING";

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private WadlUIPanel gui;

    private Project project;
    private TemplateWizard templateWizard;
    
    private JTextField fileNameTextField;
    
    private File tempWADLFile = null;
    
    private WadlModel mTempWADLModel = null;
    
    private String mErrorMessage;
    
    private TextChangeListener mListener = new TextChangeListener();
    
    WadlPanel(Project project) {
        this.project = project;
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    }
    
    void setNameTF(JTextField nameTF) {
        if(nameTF != null) {
            nameTF.getDocument().removeDocumentListener(mListener);//remove existing one
            nameTF.getDocument().addDocumentListener(mListener);
            fileNameTextField = nameTF;
        }
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new WadlUIPanel(this);
//            gui.setPreferredSize(new Dimension(450, 400));
        }
        return gui;
    }
 
    public Project getProject(){
        return project;
    }    
    
    public HelpCtx getHelp() {
        return new HelpCtx(WadlPanel.class);
    }
    
    public boolean isValid() {
        if(templateWizard != null) {
/*            String errorMessage = null;
            //This should be good enough to disable html code.
            // If not try to use the StringEscapeUtils.escapeHtml from common lang.
            if (mErrorMessage != null) {
                errorMessage = "<html>" + Utility.escapeHtml(mErrorMessage) + "</html>";
            }*/
            
            templateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, mErrorMessage); // NOI18N
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
        templateWizard.putProperty(WizardNewWadlStep.TEMP_WADLFILE, null);
        tempWADLFile = null;
        
    }

    public void storeSettings(Object settings) {
        TemplateWizard wiz = (TemplateWizard) settings;
        Object option = wiz.getValue();
        if(option == NotifyDescriptor.CANCEL_OPTION || option == WizardDescriptor.PREVIOUS_OPTION) {
            return;
        }
        
        String fileName = Templates.getTargetName(wiz);
        if (fileName == null) return;
        wiz.putProperty(FILE_NAME, fileName);
        String serviceUrl = gui.getServiceUrl();
        wiz.putProperty(WADL_SERVICEURL, serviceUrl);
        String definitionName = fileName;
        wiz.putProperty(WADL_DEFINITION_NAME, definitionName);
        try {
            if (tempWADLFile == null) {
                
                // Create a temporary file for storing our settings.
                tempWADLFile = File.createTempFile(fileName + "RIT", ".wadl"); // NOI18N
                populateFileFromTemplate(tempWADLFile);
                tempWADLFile.deleteOnExit();
                templateWizard.putProperty(
                        WizardNewWadlStep.TEMP_WADLFILE, tempWADLFile);
                //TODO
                mTempWADLModel = getModel(FileUtil.toFileObject(tempWADLFile));
                wiz.putProperty(
                        WizardNewWadlStep.TEMP_WADLMODEL, mTempWADLModel);
            } else {
                //TODO
                wiz.putProperty(
                        WizardNewWadlStep.TEMP_WADLMODEL, mTempWADLModel);
                wiz.putProperty(
                        WizardNewWadlStep.TEMP_WADLFILE, tempWADLFile);
                try {
                    mTempWADLModel.startTransaction();
                    mTempWADLModel.getApplication().getResources().iterator().next().setBase(serviceUrl);
                } finally {
                    mTempWADLModel.endTransaction();
                }
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Create a temporary WADL file that is based on the wizard template.
     *
     * @param  wizard  the template wizard.
     * @param  file    the WADL file to be written.
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
            FileOutputStream stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream, encoding);
            writer.write(stringBuilder.toString().replaceAll("#SERVICE_URL", gui.getServiceUrl()));
            writer.close();
            stream.close();
        }
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
//    
//    private boolean isValidName(Document doc) {
//        try {
//            String text = doc.getText(0, doc.getLength());
//            boolean isValid  = org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(text);
//            if(!isValid) {
//                mErrorMessage = NbBundle.getMessage(OperationPanel.class, "ERR_MSG_INVALID_NAME" , text);
//            } else {
//                mErrorMessage = null;
//            }
//            
//        }  catch(Exception ex) {
//            ex.printStackTrace();
//        }
//        
//        return mErrorMessage == null;
//    }
//    
//    
//    
    private void validateFileName() {
//        boolean validFileName = isValidName(this.fileNameTextField.getDocument());
//        if(!validFileName) {
//            fireChangeEvent();
//            return;
//        }
    }
    
    public WadlModel getModel(FileObject wadlFile) {
        ModelSource modelSource = Utilities.getModelSource(wadlFile, true);
        if(modelSource != null) {
            return WadlModelFactory.getDefault().getModel(modelSource);
        }

        return null;
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
 
}
