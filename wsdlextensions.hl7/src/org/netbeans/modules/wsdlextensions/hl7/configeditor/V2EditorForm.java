/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.hl7.HL7Constants;
import org.netbeans.modules.wsdlextensions.hl7.HL7ProtocolProperties;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.HL7BindingsConfigurationEditorPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.V2Panel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class V2EditorForm extends V2Panel implements Form {

    private static final Logger logger = Logger.getLogger(V2EditorForm.class.getName());
    private static ResourceBundle validationErrMessages = ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.hl7.validator.Bundle");
    
    private final Model model;
    private final String default_processingId = "P";
    private final String default_versionId = "2.3.1";
    private final String default_fieldSeparator = "124";
    private final String default_encodingChars = "^~&amp;";
    private final String default_sendingApplication = "Sun HL7 Binding Component";
    private final String default_sendingFacility = "Sun HL7 Binding Component";
    private final String default_softVendorOrg = "Sun Microsystems, Inc";
    private final String default_softVersionReleaseNo = "2.0";
    private final String default_softProdName = "Sun HL7 Binding Component";
    private final String default_softBinaryId = "2.0";
    private final String default_softProductInfo = "It is a binding component for HL7 over TCP/IP connection";
    private String templateConst;
    private HL7BindingsConfigurationEditorPanel holdingPanel = null;

    public V2EditorForm(Model model, String templateConst) {
        super();
        this.model = model;
        this.templateConst = templateConst;
        init();
        addEventHandlers();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //mHostTextField.requestFocusInWindow();
            }
        });

    }

    public void setHoldingPanel(HL7BindingsConfigurationEditorPanel holdingPanel) {
        this.holdingPanel = holdingPanel;
    }

    public void doFirePropertyChange(String propertyName,
            Object oldValue, Object newValue) {
        if (this.holdingPanel == null) {
            firePropertyChange(propertyName, oldValue, newValue);
        } else {
            this.holdingPanel.doFirePropertyChange(propertyName, oldValue, newValue);
        }
    }

    private void init() {

        if (HL7Constants.TEMPLATE_OUT.equals(this.templateConst)) {
            lblEncodingChars.setVisible(false);
            txtEncodingChars.setVisible(false);
            lblFieldSeparator.setVisible(false);
            txtFieldSeparator.setVisible(false);
        }


        radOriginal.setSelected(true);
        chkValidateMSH.setSelected(false);

        DefaultComboBoxModel cmbProcessingIdModel = new DefaultComboBoxModel(new String[]{
                    HL7ProtocolProperties.ProcessingId.D.getName(),
                    HL7ProtocolProperties.ProcessingId.T.getName(),
                    HL7ProtocolProperties.ProcessingId.P.getName()
                });
        cmbProcessingIdModel.setSelectedItem(HL7ProtocolProperties.ProcessingId.P.getName());
        cmbProcessingId.setModel(cmbProcessingIdModel);
        chkSeqNoEnabled.setSelected(false);

        DefaultComboBoxModel cmbVersionIdModel = new DefaultComboBoxModel(new String[]{
                    HL7ProtocolProperties.VersionId.V2_1.getName(),
                    HL7ProtocolProperties.VersionId.V2_2.getName(),
                    HL7ProtocolProperties.VersionId.V2_3.getName(),
                    HL7ProtocolProperties.VersionId.V2_3_1.getName(),
                    HL7ProtocolProperties.VersionId.V2_4.getName(),
                    HL7ProtocolProperties.VersionId.V2_5.getName(),
                    HL7ProtocolProperties.VersionId.V2_5_1.getName(),
                    HL7ProtocolProperties.VersionId.V2_6.getName(),
                });
        cmbVersionIdModel.setSelectedItem(HL7ProtocolProperties.VersionId.V2_3_1.getName());
        cmbVersionId.setModel(cmbVersionIdModel);
        txtFieldSeparator.setText(default_fieldSeparator);
        txtEncodingChars.setText(default_encodingChars);
        txtSendingApplication.setText(default_sendingApplication);
        txtSendingFacility.setText(default_sendingFacility);
        chkSFTEnabled.setSelected(false);
        txtSoftVendorOrg.setText(default_softVendorOrg);
        txtSoftVersionReleaseNo.setText(default_softVersionReleaseNo);
        txtSoftProdName.setText(default_softProdName);
        txtSoftBinaryId.setText(default_softBinaryId);
        txtSoftProductInfo.setText(default_softProductInfo);
        //CheckBox for Journalling setting to default value
        chkJournallingEnabled.setSelected(false);
    }
    
    private void addEventHandlers(){
        ActionListener actionListener = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                validateMe(true);
            }
        };
        radEnhanced.addActionListener(actionListener);
        radOriginal.addActionListener(actionListener);
        
        MyDocumentListener docListener = new MyDocumentListener();
        txtFieldSeparator.getDocument().addDocumentListener(docListener);
        txtSoftInstallDate.getDocument().addDocumentListener(docListener);
        
        chkSFTEnabled.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                validateMe(true);
            }
        });
        cmbVersionId.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
               validateMe(true);
            }
        });
        
        /*chkJournallingEnabled.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                validateMe(true);
            }
        });*/
        
        FocusListener focusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDescriptionArea(e);
            }
        };
        radEnhanced.addFocusListener(focusListener);
        radOriginal.addFocusListener(focusListener);
        chkValidateMSH.addFocusListener(focusListener);
        chkSeqNoEnabled.addFocusListener(focusListener);
        cmbProcessingId.addFocusListener(focusListener);
        cmbVersionId.addFocusListener(focusListener);
        txtSendingApplication.addFocusListener(focusListener);
        txtSendingFacility.addFocusListener(focusListener);
        txtEncodingChars.addFocusListener(focusListener);
        txtFieldSeparator.addFocusListener(focusListener);
        chkSFTEnabled.addFocusListener(focusListener);
        txtSoftVendorOrg.addFocusListener(focusListener);
        txtSoftVersionReleaseNo.addFocusListener(focusListener);
        txtSoftProdName.addFocusListener(focusListener);
        txtSoftProductInfo.addFocusListener(focusListener);
        txtSoftBinaryId.addFocusListener(focusListener);
        txtSoftInstallDate.addFocusListener(focusListener);
        chkJournallingEnabled.addFocusListener(focusListener);
        
    }

    private class MyDocumentListener implements DocumentListener{
            public void insertUpdate(DocumentEvent e) {
                validateMe(true);
            }

            public void removeUpdate(DocumentEvent e) {
                validateMe(true);
            }

            public void changedUpdate(DocumentEvent e) {
                validateMe(true);
            }
    }
    
    private void updateDescriptionArea(FocusEvent evt){
        this.descriptionPanel.setText("");

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == radEnhanced) {
            desc = new String[]{"Enhanced\n\n",
                    radEnhanced.getToolTipText()};
            casaEdited = true;
        } else if(evt.getSource() == radOriginal){
            desc = new String[]{"Original\n\n",radOriginal.getToolTipText()};
        } else if(evt.getSource() == chkValidateMSH){
            desc = new String[]{"Validate MSH\n\n",chkValidateMSH.getToolTipText()};
        } else if(evt.getSource() == chkSeqNoEnabled){
            desc = new String[]{"Sequence No\n\n",chkSeqNoEnabled.getToolTipText()};
        } else if(evt.getSource() == cmbProcessingId){
            desc = new String[]{"Processing Id\n\n",cmbProcessingId.getToolTipText()};
        } else if(evt.getSource() == cmbVersionId){
            desc = new String[]{"Version Id\n\n",cmbVersionId.getToolTipText()};
        } else if(evt.getSource() == txtSendingApplication){
            desc = new String[]{"Sending Application\n\n",txtSendingApplication.getToolTipText()};
        } else if(evt.getSource() == txtSendingFacility){
            desc = new String[]{"Sending Facility\n\n",txtSendingFacility.getToolTipText()};
        } else if(evt.getSource() == txtEncodingChars){
            desc = new String[]{"Encoding Characters\n\n",txtEncodingChars.getToolTipText()};
        } else if(evt.getSource() == txtFieldSeparator){
            desc = new String[]{"Field Separator\n\n",txtFieldSeparator.getToolTipText()};
        } else if(evt.getSource() == chkSFTEnabled){
            desc = new String[]{"SFT Enabled\n\n",chkSFTEnabled.getToolTipText()};
        } else if(evt.getSource() == txtSoftVendorOrg){
            desc = new String[]{"Software Vendor Organization\n\n",txtSoftVendorOrg.getToolTipText()};
        } else if(evt.getSource() == txtSoftVersionReleaseNo){
            desc = new String[]{"Software Certified Version or Release Number\n\n",txtSoftVersionReleaseNo.getToolTipText()};
        } else if(evt.getSource() == txtSoftProdName){
            desc = new String[]{"Software Product Name\n\n",txtSoftProdName.getToolTipText()};
        } else if(evt.getSource() == txtSoftProductInfo){
            desc = new String[]{"Software Product Information\n\n",txtSoftProductInfo.getToolTipText()};
        } else if(evt.getSource() == txtSoftBinaryId){
            desc = new String[]{"Software Binary Id\n\n",txtSoftBinaryId.getToolTipText()};
        } else if(evt.getSource() == txtSoftInstallDate){
            desc = new String[]{"Software Install Date\n\n",txtSoftInstallDate.getToolTipText()};
        } else if(evt.getSource() == chkJournallingEnabled){
            desc = new String[]{"Journalling Enabled\n\n",chkJournallingEnabled.getToolTipText()};
        }
        
        if (desc != null) {
                // Insert the image
                if (casaEdited) {
                }
                this.descriptionPanel.setText(desc[0], desc[1]);
                return;
        }

    }
        
    /**
     * Signal for the form to reread its data model into its view, in effect
     * discarding uncommitted changes made thru the view.
     */
    public void refresh() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Utils.dispatchToSwingThread("refresh()", new Runnable() {

                public void run() {
                    refresh();
                }
            });
            return;
        }
        if (HL7ProtocolProperties.AckMode.ORIGINAL.getName().equals(model.getAcknowledgementMode())) {
            radOriginal.setSelected(true);
        } else {
            radEnhanced.setSelected(true);
        }
        chkValidateMSH.setSelected(model.isValidateMSH());
        DefaultComboBoxModel cmbProcessingIdModel = new DefaultComboBoxModel(new String[]{
                    HL7ProtocolProperties.ProcessingId.D.getName(),
                    HL7ProtocolProperties.ProcessingId.T.getName(),
                    HL7ProtocolProperties.ProcessingId.P.getName()
                });
        cmbProcessingIdModel.setSelectedItem(model.getProcessingId());
        cmbProcessingId.setModel(cmbProcessingIdModel);
        chkSeqNoEnabled.setSelected(model.isSequenceNoEnabled());
        DefaultComboBoxModel cmbVersionIdModel = new DefaultComboBoxModel(new String[]{
                    HL7ProtocolProperties.VersionId.V2_1.getName(),
                    HL7ProtocolProperties.VersionId.V2_2.getName(),
                    HL7ProtocolProperties.VersionId.V2_3.getName(),
                    HL7ProtocolProperties.VersionId.V2_3_1.getName(),
                    HL7ProtocolProperties.VersionId.V2_4.getName(),
                    HL7ProtocolProperties.VersionId.V2_5.getName(),
                    HL7ProtocolProperties.VersionId.V2_5_1.getName(),
                    HL7ProtocolProperties.VersionId.V2_6.getName(),
                });
        cmbVersionIdModel.setSelectedItem(model.getVersionId());
        cmbVersionId.setModel(cmbVersionIdModel);
        txtSendingApplication.setText(model.getSendingApplication());
        txtSendingFacility.setText(model.getSendingFacility());
        txtEncodingChars.setText(model.getEncodingCharacters());
        txtFieldSeparator.setText((model.getFieldSeparator() != null ? model.getFieldSeparator().toString() : ""));
        chkSFTEnabled.setSelected(model.isSFTEnabled());
        txtSoftVendorOrg.setText(model.getSoftwareVendorOrganization());
        txtSoftVersionReleaseNo.setText(model.getSoftwareCertifiedVersionOrReleaseNo());
        txtSoftProdName.setText(model.getSoftwareProductName());
        txtSoftProductInfo.setText(model.getSoftwareProductInformation());
        txtSoftBinaryId.setText(model.getSoftwareBinaryId());
        txtSoftInstallDate.setText(model.getSoftwareInstallDate());
        chkJournallingEnabled.setSelected(model.isJournallingEnabled());
    }

    public HL7Error validateMe(boolean fireEvent) {
        HL7Error error = validateAckMode(fireEvent);
        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
            error = validateSFTEnabled(fireEvent);
            if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                error = validateFieldSeparator(fireEvent);
                if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                    error = validateSoftwareinstallDate(fireEvent);
                }
            }
        }
        return error;
    }

    /**
     * Signal for the form to update its data model with uncommitted changes
     * made thru its view.
     */
    public void commit() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Utils.dispatchToSwingThread("commit()", new Runnable() {

                public void run() {
                    commit();
                }
            });
            return;
        }

        model.setAcknowledgementMode(radOriginal.isSelected() ? HL7ProtocolProperties.AckMode.ORIGINAL.getName() : HL7ProtocolProperties.AckMode.ENHANCED.getName());
        model.setValidateMSH(chkValidateMSH.isSelected());
        model.setSequenceNoEnabled(chkSeqNoEnabled.isSelected());
        model.setProcessingId(cmbProcessingId.getSelectedItem().toString());
        model.setVersionId(cmbVersionId.getSelectedItem().toString());
        model.setSendingApplication(txtSendingApplication.getText().trim());
        model.setSendingFacility(txtSendingFacility.getText().trim());
        model.setEncodingCharacters(txtEncodingChars.getText().trim());
        try {
            model.setFieldSeparator(Byte.valueOf(txtFieldSeparator.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warning("Invalid Value provided for \"Field Separator\"");
        }
        model.setSFTEnabled(chkSFTEnabled.isSelected());
        model.setSoftwareVendorOrganization(txtSoftVendorOrg.getText().trim());
        model.setSoftwareCertifiedVersionOrReleaseNo(txtSoftVersionReleaseNo.getText().trim());
        model.setSoftwareProductName(txtSoftProdName.getText().trim());
        model.setSoftwareProductInformation(txtSoftProductInfo.getText().trim());
        model.setSoftwareBinaryId(txtSoftBinaryId.getText().trim());
        model.setSoftwareInstallDate(txtSoftInstallDate.getText().trim());
        model.setJournallingEnabled(chkJournallingEnabled.isSelected());
    }

    /**
     * Populate the form's internal data model with the information provided. If
     * the supplied model is not a type that is recognized or meaningful, it is
     * disregarded.
     *
     * @param model A supported FormModel instance.
     */
    public void loadModel(FormModel model) {
    }

    /**
     * Returns the form's own data model.
     *
     * @return Form data model
     */
    public FormModel getModel() {
        return model;
    }

    /**
     * The Swing component that represents the form's visual representation.
     *
     * @return The form's view.
     */
    public JComponent getComponent() {
        return this;
    }

    public static void syncV2Panel_ToFrom(Form.FormModel destModel, Form.FormModel srcModel)
            throws ModelModificationException {
        if (!(destModel instanceof V2EditorForm.Model)) {
            return;
        }
        if (!(srcModel instanceof V2EditorForm.Model)) {
            return;
        }
        V2EditorForm.Model dest =
                (V2EditorForm.Model) destModel;
        V2EditorForm.Model src =
                (V2EditorForm.Model) srcModel;

        dest.setAcknowledgementMode(src.getAcknowledgementMode());
        dest.setValidateMSH(src.isValidateMSH());
        dest.setProcessingId(src.getProcessingId());
        dest.setVersionId(src.getVersionId());
        dest.setSequenceNoEnabled(src.isSequenceNoEnabled());
        dest.setSendingApplication(src.getSendingApplication());
        dest.setSendingFacility(src.getSendingFacility());
        dest.setEncodingCharacters(src.getEncodingCharacters());
        dest.setFieldSeparator(src.getFieldSeparator());
        dest.setSFTEnabled(src.isSFTEnabled());
        dest.setSoftwareVendorOrganization(src.getSoftwareVendorOrganization());
        dest.setSoftwareCertifiedVersionOrReleaseNo(src.getSoftwareCertifiedVersionOrReleaseNo());
        dest.setSoftwareProductName(src.getSoftwareProductName());
        dest.setSoftwareProductInformation(src.getSoftwareProductInformation());
        dest.setSoftwareBinaryId(src.getSoftwareBinaryId());
        dest.setSoftwareInstallDate(src.getSoftwareInstallDate());
        dest.setJournallingEnabled(src.isJournallingEnabled());

    }

    private HL7Error validateAckMode(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String versionId = cmbVersionId.getSelectedItem().toString();
        if (radEnhanced.isSelected() && versionId.equals(HL7ProtocolProperties.VersionId.V2_1.getName())) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_ACKCODE_FOR_VERSION",new Object[]{cmbVersionId.getSelectedItem(),radEnhanced.getText()}));
        }
        
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }        
        return error;
    }

    private HL7Error validateFieldSeparator(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String fieldSeparator = txtFieldSeparator.getText();

        try {
            byte b = Byte.parseByte(fieldSeparator);
        } catch (NumberFormatException numberFormatException) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_HL7_FIELDSEPARATOR_CHARACTER"));
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
        
    }

    private HL7Error validateSFTEnabled(boolean fireEvent) {
        HL7Error error = new HL7Error();
        
        String versionID = cmbVersionId.getSelectedItem().toString();
        if(chkSFTEnabled.isSelected() && 
                !(versionID.equals(HL7ProtocolProperties.VersionId.V2_5.getName()) || versionID.equals(HL7ProtocolProperties.VersionId.V2_5_1.getName())
						|| versionID.equals(HL7ProtocolProperties.VersionId.V2_6.getName()))){
            
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_VERSION_FOR_SFT", new Object[]{versionID}));
        } 
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
        
        return error;
    }

    private HL7Error validateSoftwareinstallDate(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String softwareInstallDate = txtSoftInstallDate.getText().trim();
        
        if(!"".equals(softwareInstallDate)){
            Date dt = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                sdf.setLenient(false);
                dt = sdf.parse(softwareInstallDate);
            } catch (ParseException e) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_DATE", new Object[]{softwareInstallDate}));
            }
        }
        
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
        
        return error;
    }

    private String getValidationErrorMessage(String key) {
        return getValidationErrorMessage(key, null);
    }

    private String getValidationErrorMessage(String key, Object[] params) {
        String fmt = validationErrMessages.getString(key);
        if (params != null) {
            return MessageFormat.format(fmt, params);
        } else {
            return fmt;
        }
    }

    /**
     * Data model that this view/panel can understand. Implement this interface
     * to supply this panel with content.
     */
    public interface Model extends FormModel {

        String getAcknowledgementMode();

        void setAcknowledgementMode(String ackMode);

        String getProcessingId();

        void setProcessingId(String processingId);

        boolean isSFTEnabled();

        void setSFTEnabled(boolean enabled);

        boolean isSequenceNoEnabled();

        void setSequenceNoEnabled(boolean enabled);

        String getSoftwareBinaryId();

        void setSoftwareBinaryId(String id);

        String getSoftwareCertifiedVersionOrReleaseNo();

        void setSoftwareCertifiedVersionOrReleaseNo(String version);

        String getSoftwareProductInformation();

        void setSoftwareProductInformation(String info);

        String getSoftwareProductName();

        void setSoftwareProductName(String name);

        String getSoftwareVendorOrganization();

        void setSoftwareVendorOrganization(String name);

        boolean isValidateMSH();

        void setValidateMSH(boolean enabled);

        String getVersionId();

        void setVersionId(String versionId);

        Byte getFieldSeparator();

        void setFieldSeparator(Byte fieldSeparator);

        String getSendingApplication();

        void setSendingApplication(String sendingApplication);

        String getSendingFacility();

        void setSendingFacility(String sendingFacility);

        String getSoftwareInstallDate();

        void setSoftwareInstallDate(String softwareInstallDate);

        String getEncodingCharacters();

        void setEncodingCharacters(String encodingCharacters);

        boolean isJournallingEnabled();

        void setJournallingEnabled(boolean enabled);
    }
}
