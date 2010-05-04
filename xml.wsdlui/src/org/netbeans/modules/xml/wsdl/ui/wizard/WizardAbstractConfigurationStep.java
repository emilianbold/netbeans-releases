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

/*
 * WSDLWizardConstants.java
 *
 * Created on August 31, 2006, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import org.netbeans.modules.xml.wsdl.ui.wizard.common.PortTypeGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.OperationConfigurationPanel;
import org.netbeans.modules.xml.wsdl.ui.view.OperationType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.OperationPanel;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class WizardAbstractConfigurationStep extends WSDLWizardDescriptorPanel implements WSDLWizardConstants {
    
 
    
    
    private OperationConfigurationPanel mPanel;
    
    private Project project;
    
    private String mErrorMessage;
    
    private WizardDescriptor wiz = null;
    
    private PortType mPortType;
    
    private List<Message> mNewMessageList = null;
    
    private ExtensibilityElement mPartnerLinkTypeElement = null;
    
    private Collection<Import> mImports = null;
    
    private String fileName;
    private WSDLModel mTempModel;
    private boolean isFinishable;
    
    /** Creates a new instance of WSDLWizardConstants */
    public WizardAbstractConfigurationStep(WSDLWizardContext context, boolean hasFinish) {
        super(context);
        isFinishable = hasFinish;
    }

    public String getName() {
        return NbBundle.getMessage(WizardAbstractConfigurationStep.class, "LBL_WizardPortTypeConfigurationStep");
    }
    
    

    @Override
    public Component getComponent() {
        if (mPanel == null) {
            this.mPanel = new OperationConfigurationPanel(project, false, mTempModel, true);
            this.mPanel.setPortTypeName(fileName + "PortType"); //NOI18N
            this.mPanel.setOperationName(fileName + "Operation"); //NOI18N
            TextChangeListener listener  = new TextChangeListener();
            
            this.mPanel.getPortTypeNameTextField().getDocument().addDocumentListener(listener);
            this.mPanel.getOperationNameTextField().getDocument().addDocumentListener(listener);
            this.mPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WizardAbstractConfigurationStep.class, "LBL_WizardPortTypeConfigurationStep"));
            //getWSDLWizardContext().setHasNext(false);
        }
        return this.mPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(WizardAbstractConfigurationStep.class);
    }

    @Override
    public boolean isValid() {
/*        String errorMessage = null;
        //This should be good enough to disable html code.
        // If not try to use the StringEscapeUtils.escapeHtml from common lang.
        if (mErrorMessage != null) {
            errorMessage = "<html>" + Utility.escapeHtml(mErrorMessage) + "</html>";
        }*/
        
        wiz.putProperty ("WizardPanel_errorMessage", mErrorMessage); // NOI18N
        return this.mErrorMessage == null;
        
    }

    
    @Override
    public void readSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard)settings;
        this.wiz = templateWizard;
        fileName = (String) templateWizard.getProperty(WsdlPanel.FILE_NAME);
        project = Templates.getProject(wiz);
        mTempModel = (WSDLModel) templateWizard.getProperty(WizardAbstractConfigurationStep.TEMP_WSDLMODEL);
    }

    @Override
    public void storeSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard)settings;
        WSDLModel tempModel = (WSDLModel) templateWizard.getProperty(WizardAbstractConfigurationStep.TEMP_WSDLMODEL);
        Object option = templateWizard.getValue();
        if(option == NotifyDescriptor.CANCEL_OPTION || option == WizardDescriptor.PREVIOUS_OPTION) {
            DataObject dobj = ActionHelper.getDataObject(tempModel);
            if (dobj != null) {
                dobj.setModified(false);
                try {
                    dobj.delete();
                } catch (Exception e) {
                    //ignore
                }
            }
            templateWizard.putProperty(WizardAbstractConfigurationStep.TEMP_WSDLMODEL, null);

            // fix for issue #160855 - NPE at org.netbeans.modules.xml.xdm.XDMModel.flushDocument
            templateWizard.putProperty(WSDLWizardConstants.CREATE_NEW_TEMP_WSDLFILE, true);

            mPortType = null;
            mNewMessageList = null;
            mPartnerLinkTypeElement = null;
            mImports = null;
            return;
        }

        if(tempModel != null) {
            tempModel.startTransaction();
            if(this.mPortType != null) {
                tempModel.getDefinitions().removePortType(this.mPortType);
            }

            if(this.mNewMessageList != null) {
                for (Message msg : mNewMessageList) {
                    tempModel.getDefinitions().removeMessage(msg);
                }
            }

            if(this.mPartnerLinkTypeElement != null) {
                tempModel.getDefinitions().removeExtensibilityElement(this.mPartnerLinkTypeElement);
            }

            if(this.mImports != null) {
                //Cleanup all inline schemas and remove the imported schemas from the inline schema.
                Collection<WSDLSchema> wSchemas = tempModel.getDefinitions().getTypes().getExtensibilityElements(WSDLSchema.class);
                for (WSDLSchema wSchema : wSchemas) {
                    Schema schema = wSchema.getSchemaModel().getSchema();
                    //Wizard adds all imported schemas in a inline schema with same TNS as the definitions.
                    //So remove from that schema.
                    if (schema.getTargetNamespace().equals(tempModel.getDefinitions().getTargetNamespace())) {
                        for (Import imp : mImports) {
                            schema.removeExternalReference(imp);
                        }
                    }
                    tempModel.getDefinitions().getTypes().removeExtensibilityElement(wSchema);
                }
            }

            
            mPortType = null;
            mNewMessageList = null;
            mPartnerLinkTypeElement = null;
            mImports = null;

            String portTypeName = this.mPanel.getPortTypeName();
            String operationName = this.mPanel.getOperationName();        
            OperationType ot = this.mPanel.getOperationType();
            boolean autoGenPLT = mPanel.isAutoGeneratePartnerLinkType();


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
            configurationMap.put(WizardAbstractConfigurationStep.PORTTYPE_NAME, portTypeName);
            configurationMap.put(WizardAbstractConfigurationStep.OPERATION_NAME, operationName);
            configurationMap.put(WizardAbstractConfigurationStep.OPERATION_TYPE, ot);
            configurationMap.put(WizardAbstractConfigurationStep.AUTOGEN_PARTNERLINKTYPE, autoGenPLT);

            //opertion type
            configurationMap.put(WizardAbstractConfigurationStep.OPERATION_INPUT, inputMessageParts);
            configurationMap.put(WizardAbstractConfigurationStep.OPERATION_OUTPUT, outputMessageParts);
            configurationMap.put(WizardAbstractConfigurationStep.OPERATION_FAULT, faultMessageParts);
            configurationMap.put(WizardAbstractConfigurationStep.NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
            configurationMap.put(WizardAbstractConfigurationStep.IS_FROM_WIZARD, Boolean.TRUE);

            templateWizard.putProperty(PORTTYPE_NAME, portTypeName);
            templateWizard.putProperty(OPERATION_NAME, operationName);
            templateWizard.putProperty(OPERATION_TYPE, ot);

            PortTypeGenerator ptGen = new PortTypeGenerator(tempModel, configurationMap);
            ptGen.execute();
            this.mPortType = ptGen.getPortType();
            this.mNewMessageList = ptGen.getNewMessages();
            this.mPartnerLinkTypeElement = ptGen.getPartnerLinkType();
            this.mImports = ptGen.getImports();

            tempModel.endTransaction();
            
            templateWizard.putProperty(PORTTYPE, this.mPortType);
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
    
    private void validate() {
        boolean validPortType = isValidName(this.mPanel.getPortTypeNameTextField().getDocument());
        if(!validPortType) {
            fireChange();
            return;
        }
        
        boolean validOperation = isValidName(this.mPanel.getOperationNameTextField().getDocument());
        
        if(!validOperation) {
            fireChange();
            return;
        }
        
        fireChange();
    }
    
    @Override
    public boolean isFinishPanel() {
        return isFinishable;
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
