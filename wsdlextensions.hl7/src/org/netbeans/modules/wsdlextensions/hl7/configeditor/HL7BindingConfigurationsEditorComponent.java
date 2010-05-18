/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.netbeans.modules.wsdlextensions.hl7.HL7Constants;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.HL7BindingsConfigurationEditorPanel;
import org.netbeans.modules.wsdlextensions.hl7.validator.HL7ComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Controller that manages the BindingsConfigurationEditorView and the data
 * model.
 *
 * @author Vishnuvardhan P.R
 */
public class HL7BindingConfigurationsEditorComponent 
        implements ExtensibilityElementConfigurationEditorComponent{

    private static final Logger logger = Logger.getLogger(
            HL7BindingConfigurationsEditorComponent.class.getName());
    
    private final GeneralEditorForm generalForm;
    private final V2EditorForm v2Form;
    private final CommunicationControlForm commControlForm;
    
    private final GeneralEditorWsdlAdapter generalEditorAdapter;
    private final V2EditorWsdlAdapter v2EditorAdapter;
    private final CommunicationControlWsdlAdapter commControlWsdlAdapter;
    
    private HL7BindingsConfigurationEditorPanel holdingPanel = null;   
    private String linkDirection;
    private WSDLModel wsdlModel;
    
    public HL7BindingConfigurationsEditorComponent(WSDLModel wsdlModel, String linkDirection){
        if (wsdlModel == null) {
            throw new NullPointerException("wsdlModel");
        }
        this.wsdlModel = wsdlModel;
        this.linkDirection = linkDirection;
        holdingPanel = new HL7BindingsConfigurationEditorPanel();        
        
        //For General Tab
        GeneralEditorForm.Model generalModel  = new GeneralEditorModel();
        generalEditorAdapter = new GeneralEditorWsdlAdapter(wsdlModel);
        try {
            GeneralEditorForm.syncGeneralPanel_ToFrom(generalModel, generalEditorAdapter);
        } catch (ModelModificationException e) {
            throw new RuntimeException(e);
        }
        String templateConstant = "";
        if(ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION.equals(linkDirection)){
            templateConstant = HL7Constants.TEMPLATE_IN;
        }else if(ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION.equals(linkDirection)){
            templateConstant = HL7Constants.TEMPLATE_OUT;
        }
        generalForm = new GeneralEditorForm(generalModel,templateConstant);
        holdingPanel.addTab("General Properties",(GeneralEditorForm)generalForm);
        generalForm.setHoldingPanel(holdingPanel);
        
        //For V2 Tab
        
        V2EditorForm.Model v2Model = new V2EditorModel();
        v2EditorAdapter = new V2EditorWsdlAdapter(wsdlModel);
        try {
            V2EditorForm.syncV2Panel_ToFrom(v2Model, v2EditorAdapter);
        } catch (ModelModificationException e) {
            throw new RuntimeException(e);
        }
        v2Form = new V2EditorForm(v2Model,templateConstant);
        holdingPanel.addTab("HL7 Version 2 Properties", (V2EditorForm)v2Form);
        v2Form.setHoldingPanel(holdingPanel);
        //For Communication Controls Tab
        
        CommunicationControlForm.Model commControlModel = new CommunicationControlModel();
        commControlWsdlAdapter = new CommunicationControlWsdlAdapter(wsdlModel,templateConstant);
        try {
            CommunicationControlForm.syncCommControlPanel_ToFrom(commControlModel, commControlWsdlAdapter);
        } catch (ModelModificationException e) {
            throw new RuntimeException(e);
        }
        commControlForm = new CommunicationControlForm(commControlModel,templateConstant);
        holdingPanel.addTab("Communication Controls", (CommunicationControlForm)commControlForm);
        commControlForm.setHoldingPanel(holdingPanel);

        //For V3 Tab
        /*  
       v3Model = new V3EditorModel();
        v3Form = new V3EditorForm(v3Model);

        holdingPanel.addTab("HL7 Version 3 Properties", (V3EditorForm)v3Form);

        */
    }

    /**
     * Return a editor panel. In general, its better to cache this in
     * implementation, till commit/rollback is called. So that user provided
     * values will be saved in the panel.
     *
     * @return editor view
     */
    public JPanel getEditorPanel() {
        return (JPanel) holdingPanel;
    }

    /**
     * Return title for the dialog.
     *
     * @return String title
     */
    public String getTitle() {
        return NbBundle.getMessage(HL7BindingConfigurationsEditorComponent.class,
                "HL7BindingConfigurationsEditor.CONFIGURE_TITLE"); //NOI18N

    }

    /**
     * Return the helpctx to be shown in dialog/wizards.
     *
     * @return HelpCtx.
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /**
     * Generally is not needed to be used. Implement if you have special cases.
     * listener on OK/Cancel buttons.
     *
     * @return ActionListener
     */
    public ActionListener getActionListener() {
        return null;
    }
    
    /**
     * Commit all values from the panel and commit it to the wsdl model. Return
     * true, if successfully committed, otherwise false.
     *
     * @return boolean
     */
    public boolean commit() {
        boolean success = true;
        
        /*if(validateContent()){
            return false;
        }*/
        //generalForm.validateMe(false);
        //v2Form.validateMe(false);
        //commControlForm.validateMe(false);
            //success = false;
            //return success;
        
        generalForm.commit();
        v2Form.commit();
        commControlForm.commit();
        try {
            GeneralEditorForm.syncGeneralPanel_ToFrom(generalEditorAdapter, generalForm.getModel());
            V2EditorForm.syncV2Panel_ToFrom(v2EditorAdapter, v2Form.getModel());
            CommunicationControlForm.syncCommControlPanel_ToFrom(commControlWsdlAdapter, commControlForm.getModel());
        } catch (ModelModificationException e) {
            logger.log(Level.SEVERE, "Configuration failed to save to WSDL document", e);
            success = false;
        }
        return success;
    }
    
    /**
     * Cleanup panel, discard values.
     *
     * @return boolean
     */
    public boolean rollback() {
        // Note: Different from reparse semantics.
        // Here, we tell the form to reload its presentation from its own model
        // (The Form contract requires data held by visual widgets to be
        // effectively distinct from what is in the Form's model.)
        // The underlying WSDL model may have changed, yes, but that's not
        // my problem here.  That's reparse business.
        generalForm.refresh();
        v2Form.refresh();
        commControlForm.refresh();
        return true;
    }

    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    /*public boolean validateContent() {
        HL7Error fileError = generalForm.validateMe(true);
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(fileError.getErrorMode())) {
            return false;
        }

        ValidationResult results = new HL7ComponentValidator().
                validate(this.wsdlModel, null, ValidationType.COMPLETE);
        Collection<ResultItem> resultItems = results.getValidationResult();
        ResultItem firstResult = null;
        String type = ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT;
        boolean result = true;
        if (resultItems != null && !resultItems.isEmpty()) {
            for (ResultItem item : resultItems) {
                if (item.getType() == ResultType.ERROR) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT;
                    result = false;
                    break;
                } else if (firstResult == null) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_WARNING_EVT;
                }
            }
        }
        if (firstResult != null) {
            //firePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            //firePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    //PROPERTY_CLEAR_MESSAGES_EVT, null, "");
            return true;
        }

    }*/

        
    /**
     * Do validation, and return true if valid, otherwise false.
     *
     * @return boolean
     */
    public boolean isValid() {
        /*HL7Error error1 = generalForm.validateMe(false); 
        HL7Error error2 = v2Form.validateMe(false);
        HL7Error error3 = commControlForm.validateMe(false);
        if(ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(error1.getErrorMode()) ||
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(error2.getErrorMode()) ||
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(error3.getErrorMode())){
            return false;
        }else {
            return true;
        }*/
        
        return true;
    }
    
    public void reparse(WSDLComponent component) {
        // Note: Different from rollback semantics.
        // In rollback(), we discard changes to the form made THRU the form.
        // Here, we want to reload ALL changes regardless of source;
        // essentially, repopulate the form's data model.
        try {
            generalEditorAdapter.focus(component);
            v2EditorAdapter.focus(component);
            commControlWsdlAdapter.focus(component);
            
            GeneralEditorForm.syncGeneralPanel_ToFrom(generalForm.getModel(), generalEditorAdapter);
            V2EditorForm.syncV2Panel_ToFrom(v2Form.getModel(), v2EditorAdapter);
            CommunicationControlForm.syncCommControlPanel_ToFrom(commControlForm.getModel(), commControlWsdlAdapter);
            
            generalForm.refresh();
            v2Form.refresh();
            commControlForm.refresh();
        } catch (ModelModificationException e) {
            logger.log(Level.SEVERE, "Failed to reload configuration from WSDL document", e);
        }
    }

    private boolean requiredInformationExists() {
        boolean success = true ;
        /*success = !Utils.safeString(viewModel.getQueueManager()).equals("");
        success &= !Utils.safeString(viewModel.getQueue()).equals("");
        success &= !Utils.safeString(viewModel.getPolling()).equals(""); */
        return success;
    }

}
