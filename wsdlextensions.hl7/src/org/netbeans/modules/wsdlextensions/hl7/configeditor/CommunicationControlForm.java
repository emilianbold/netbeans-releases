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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.hl7.HL7Constants;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.CommunicationControlPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.HL7BindingsConfigurationEditorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class CommunicationControlForm extends CommunicationControlPanel implements Form {

    enum RecourseActions {

        Resend("Resend"),
        Reset("Reset"),
        Suspend("Suspend"),
        Error("Error"),
        Skipmessage("Skipmessage");
        private String str;

        RecourseActions(String str) {
            this.str = str;
        }

        public String toString() {
            return this.str;
        }
    };
    private static Logger logger = Logger.getLogger(CommunicationControlForm.class.getName());
    private static ResourceBundle validationErrMessages = ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.hl7.validator.Bundle");
    private final Model model;
    private String templateConst;
    private HL7BindingsConfigurationEditorPanel holdingPanel = null;
    private static final String PATTERN = "(\\d+);(\\d+)";

    public CommunicationControlForm(Model model, String templateConst) {
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

    private void init() {
        if (HL7Constants.TEMPLATE_IN.equals(this.templateConst)) {
            pnlOutbound.setVisible(false);
        } else if (HL7Constants.TEMPLATE_OUT.equals(this.templateConst)) {
            pnlInbound.setVisible(false);
        }

        initComboBoxes();
    }

    private void initComboBoxes() {
        String[] timeToWaitActions = new String[]{
            "",
            RecourseActions.Resend.toString(),
            RecourseActions.Reset.toString(),
            RecourseActions.Suspend.toString()
        };

        String[] nakReceivedActions = new String[]{
            "",
            RecourseActions.Resend.toString(),
            RecourseActions.Reset.toString(),
            RecourseActions.Skipmessage.toString()
        };

        String[] maxNoResponseActions = new String[]{
            "",
            RecourseActions.Suspend.toString(),
            RecourseActions.Reset.toString()
        };

        String[] maxNakReceivedActions = new String[]{
            "",
            RecourseActions.Suspend.toString(),
            RecourseActions.Reset.toString(),
            RecourseActions.Skipmessage.toString()
        };

        String[] maxNakSentActions = new String[]{
            "",
            RecourseActions.Suspend.toString(),
            RecourseActions.Reset.toString()
        };

        String[] maxCannedNakSentActions = new String[]{
            "",
            RecourseActions.Suspend.toString(),
            RecourseActions.Reset.toString()
        };

        String[] maxConnectRetriesActions = new String[]{
            "",
            RecourseActions.Suspend.toString(),
            RecourseActions.Error.toString()
        };

        cmbTimeToWait.setModel(new DefaultComboBoxModel(timeToWaitActions));
        cmbTimeToWait.setSelectedItem("");
        cmbNakReceived.setModel(new DefaultComboBoxModel(nakReceivedActions));
        cmbNakReceived.setSelectedItem("");
        cmbMaxNoResponse.setModel(new DefaultComboBoxModel(maxNoResponseActions));
        cmbMaxNoResponse.setSelectedItem("");
        cmbMaxNakReceived.setModel(new DefaultComboBoxModel(maxNakReceivedActions));
        cmbMaxNakReceived.setSelectedItem("");
        cmbMaxCanNakSent.setModel(new DefaultComboBoxModel(maxCannedNakSentActions));
        cmbMaxCanNakSent.setSelectedItem("");
        cmbMaxNakSent.setModel(new DefaultComboBoxModel(maxNakSentActions));
        cmbMaxNakSent.setSelectedItem("");
        cmbMaxConnectRetries.setModel(new DefaultComboBoxModel(maxConnectRetriesActions));
        cmbMaxConnectRetries.setSelectedItem("");


    }

    private void addEventHandlers() {
        ItemListener itemListener = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                validateMe(true);
            }
        };
        DocumentListener docListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                validateMe(true);
            }

            public void removeUpdate(DocumentEvent e) {
                validateMe(true);
            }

            public void changedUpdate(DocumentEvent e) {
                validateMe(true);
            }
        };

        chkMaxCanNakSent.addItemListener(itemListener);
        chkMaxConnectRetries.addItemListener(itemListener);
        chkMaxNakReceived.addItemListener(itemListener);
        chkMaxNakSent.addItemListener(itemListener);
        chkMaxNoResponse.addItemListener(itemListener);
        chkNakReceived.addItemListener(itemListener);
        chkTimeToWait.addItemListener(itemListener);

        cmbMaxCanNakSent.addItemListener(itemListener);
        cmbMaxConnectRetries.addItemListener(itemListener);
        cmbMaxNakReceived.addItemListener(itemListener);
        cmbMaxNakSent.addItemListener(itemListener);
        cmbMaxNoResponse.addItemListener(itemListener);
        cmbNakReceived.addItemListener(itemListener);
        cmbTimeToWait.addItemListener(itemListener);

        txtMaxCanNakSent.getDocument().addDocumentListener(docListener);
        txtMaxConnectRetries.getDocument().addDocumentListener(docListener);
        txtMaxNakReceived.getDocument().addDocumentListener(docListener);
        txtMaxNakSent.getDocument().addDocumentListener(docListener);
        txtMaxNoResponse.getDocument().addDocumentListener(docListener);
        txtTimeToWait.getDocument().addDocumentListener(docListener);
        
        FocusListener focusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDescriptionArea(e);
            }
        };
        chkMaxCanNakSent.addFocusListener(focusListener);
        chkMaxConnectRetries.addFocusListener(focusListener);
        chkMaxNakReceived.addFocusListener(focusListener);
        chkMaxNakSent.addFocusListener(focusListener);
        chkMaxNoResponse.addFocusListener(focusListener);
        chkNakReceived.addFocusListener(focusListener);
        chkTimeToWait.addFocusListener(focusListener);

        cmbMaxCanNakSent.addFocusListener(focusListener);
        cmbMaxConnectRetries.addFocusListener(focusListener);
        cmbMaxNakReceived.addFocusListener(focusListener);
        cmbMaxNakSent.addFocusListener(focusListener);
        cmbMaxNoResponse.addFocusListener(focusListener);
        cmbNakReceived.addFocusListener(focusListener);
        cmbTimeToWait.addFocusListener(focusListener);

        txtMaxCanNakSent.addFocusListener(focusListener);
        txtMaxConnectRetries.addFocusListener(focusListener);
        txtMaxNakReceived.addFocusListener(focusListener);
        txtMaxNakSent.addFocusListener(focusListener);
        txtMaxNoResponse.addFocusListener(focusListener);
        txtTimeToWait.addFocusListener(focusListener);

    }
    private void updateDescriptionArea(FocusEvent evt){
        this.descriptionPanel.setText("");

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == chkMaxNakSent) {
            desc = new String[]{"Max Nak Sent\n\n",
                    chkMaxNakSent.getToolTipText()};
            casaEdited = true;
        } else if(evt.getSource() == txtMaxNakSent){
            desc = new String[]{"Max Nak Sent\n\n",txtMaxNakSent.getToolTipText()};
        } else if(evt.getSource() == cmbMaxNakSent){
            desc = new String[]{"Max Nak Sent\n\n",cmbMaxNakSent.getToolTipText()};
        } else if(evt.getSource() == chkMaxCanNakSent){
            desc = new String[]{"Max Canned Nak Sent\n\n",chkMaxCanNakSent.getToolTipText()};
        } else if(evt.getSource() == txtMaxCanNakSent){
            desc = new String[]{"Max Canned Nak Sent\n\n",txtMaxCanNakSent.getToolTipText()};
        } else if(evt.getSource() == cmbMaxCanNakSent){
            desc = new String[]{"Max Canned Nak Sent\n\n",chkMaxCanNakSent.getToolTipText()};
        } else if(evt.getSource() == chkMaxConnectRetries){
            desc = new String[]{"Max Connect Retries\n\n",chkMaxConnectRetries.getToolTipText()};
        } else if(evt.getSource() == txtMaxConnectRetries){
            desc = new String[]{"Max Connect Retries\n\n",txtMaxConnectRetries.getToolTipText()};
        } else if(evt.getSource() == cmbMaxConnectRetries){
            desc = new String[]{"Max Connect Retries\n\n",cmbMaxConnectRetries.getToolTipText()};
        } else if(evt.getSource() == chkTimeToWait){
            desc = new String[]{"Time to Wait for Response\n\n",chkTimeToWait.getToolTipText()};
        } else if(evt.getSource() == txtTimeToWait){
            desc = new String[]{"Time to Wait for Response\n\n",txtTimeToWait.getToolTipText()};
        } else if(evt.getSource() == cmbTimeToWait){
            desc = new String[]{"Time to Wait for Response\n\n",cmbTimeToWait.getToolTipText()};
        } else if(evt.getSource() == chkMaxNoResponse){
            desc = new String[]{"Max No Response\n\n",chkMaxNoResponse.getToolTipText()};
        } else if(evt.getSource() == txtMaxNoResponse){
            desc = new String[]{"Max No Response\n\n",txtMaxNoResponse.getToolTipText()};
        } else if(evt.getSource() == cmbMaxNoResponse){
            desc = new String[]{"Max No Response\n\n",cmbMaxNoResponse.getToolTipText()};
        } else if(evt.getSource() == chkNakReceived){
            desc = new String[]{"Nak Received\n\n",chkNakReceived.getToolTipText()};
        } else if(evt.getSource() == cmbNakReceived){
            desc = new String[]{"Nak Received\n\n",cmbNakReceived.getToolTipText()};
        }  else if(evt.getSource() == chkMaxNakReceived){
            desc = new String[]{"Max Nak Received\n\n",chkMaxNakReceived.getToolTipText()};
        } else if(evt.getSource() == txtMaxNakReceived){
            desc = new String[]{"Max Nak Received\n\n",txtMaxNakReceived.getToolTipText()};
        } else if(evt.getSource() == cmbMaxNakReceived){
            desc = new String[]{"Max Nak Received\n\n",cmbMaxNakReceived.getToolTipText()};
        } 
        
        if (desc != null) {
                // Insert the image
                if (casaEdited) {
                }
                this.descriptionPanel.setText(desc[0], desc[1]);
                return;
        }

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
        initComboBoxes();
        Model.MaxNakSentControl maxNakSentCtrl = model.getMaxNakSentControl();
        if(maxNakSentCtrl != null){
            chkMaxNakSent.setSelected(maxNakSentCtrl.isEnabled());
            txtMaxNakSent.setText(maxNakSentCtrl.getValue().toString());
            cmbMaxNakSent.setSelectedItem(maxNakSentCtrl.getRecourseAction());
        }

        Model.MaxCannedNakSentControl maxCannedNakSentCtrl = model.getMaxCannedNakSentControl();
        if(maxCannedNakSentCtrl != null){
            chkMaxCanNakSent.setSelected(maxCannedNakSentCtrl.isEnabled());
            txtMaxCanNakSent.setText(maxCannedNakSentCtrl.getValue().toString());
            cmbMaxCanNakSent.setSelectedItem(maxCannedNakSentCtrl.getRecourseAction());
        }

        Model.MaxConnectRetriesControl maxConnectRetriesCtrl = model.getMaxConnectRetriesControl();
        if(maxConnectRetriesCtrl != null){
            chkMaxConnectRetries.setSelected(maxConnectRetriesCtrl.isEnabled());
            txtMaxConnectRetries.setText(maxConnectRetriesCtrl.getValueAsString().toString());
            cmbMaxConnectRetries.setSelectedItem(maxConnectRetriesCtrl.getRecourseAction());
        }

        Model.TimeToWaitControl timeToWaitCtrl = model.getTimeToWaitControl();
        if(timeToWaitCtrl != null){
            chkTimeToWait.setSelected(timeToWaitCtrl.isEnabled());
            txtTimeToWait.setText(timeToWaitCtrl.getValue().toString());
            cmbTimeToWait.setSelectedItem(timeToWaitCtrl.getRecourseAction());
        }

        Model.MaxNoResponseControl maxNoResponseCtrl = model.getMaxNoResponseControl();
        if(maxNoResponseCtrl != null){
            chkMaxNoResponse.setSelected(maxNoResponseCtrl.isEnabled());
            txtMaxNoResponse.setText(maxNoResponseCtrl.getValue().toString());
            cmbMaxNoResponse.setSelectedItem(maxNoResponseCtrl.getRecourseAction());
        }

        Model.NakReceivedControl nakReceivedCtrl = model.getNakReceivedControl();
        if(nakReceivedCtrl != null){
            chkNakReceived.setSelected(nakReceivedCtrl.isEnabled());
            //txtNakReceived.setText(nakReceivedCtrl.getValue().toString());
            cmbNakReceived.setSelectedItem(nakReceivedCtrl.getRecourseAction());
        }

        Model.MaxNakReceivedControl maxNakReceivedCtrl = model.getMaxNakReceivedControl();
        if(maxNakReceivedCtrl != null){
            chkMaxNakReceived.setSelected(maxNakReceivedCtrl.isEnabled());
            txtMaxNakReceived.setText(maxNakReceivedCtrl.getValue().toString());
            cmbMaxNakReceived.setSelectedItem(maxNakReceivedCtrl.getRecourseAction());
        }

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

        Model.TimeToWaitControl timetoWaitControl = new Model.TimeToWaitControl();
        timetoWaitControl.setEnabled(chkTimeToWait.isSelected());
        timetoWaitControl.setValue(toLong(txtTimeToWait.getText().trim()));
        timetoWaitControl.setRecourseAction(cmbTimeToWait.getSelectedItem().toString());
        model.setTimeToWaitControl(timetoWaitControl);

        Model.MaxCannedNakSentControl maxCannedNakSentControl = new Model.MaxCannedNakSentControl();
        maxCannedNakSentControl.setEnabled(chkMaxCanNakSent.isSelected());
        maxCannedNakSentControl.setValue(toLong(txtMaxCanNakSent.getText().trim()));
        maxCannedNakSentControl.setRecourseAction(cmbMaxCanNakSent.getSelectedItem().toString());
        model.setMaxCannedNakSentControl(maxCannedNakSentControl);

        Model.MaxConnectRetriesControl maxConnectRetriesControl = new Model.MaxConnectRetriesControl();
        maxConnectRetriesControl.setEnabled(chkMaxConnectRetries.isSelected());
        maxConnectRetriesControl.setValueAsString(txtMaxConnectRetries.getText());
        maxConnectRetriesControl.setRecourseAction(cmbMaxConnectRetries.getSelectedItem().toString());
        model.setMaxConnectRetriesControl(maxConnectRetriesControl);

        Model.MaxNakReceivedControl maxNakReceivedControl = new Model.MaxNakReceivedControl();
        maxNakReceivedControl.setEnabled(chkMaxNakReceived.isSelected());
        maxNakReceivedControl.setValue(toLong(txtMaxNakReceived.getText().trim()));
        maxNakReceivedControl.setRecourseAction(cmbMaxNakReceived.getSelectedItem().toString());
        model.setMaxNakReceivedControl(maxNakReceivedControl);

        Model.MaxNakSentControl maxNakSentControl = new Model.MaxNakSentControl();
        maxNakSentControl.setEnabled(chkMaxNakSent.isSelected());
        maxNakSentControl.setValue(toLong(txtMaxNakSent.getText().trim()));
        maxNakSentControl.setRecourseAction(cmbMaxNakSent.getSelectedItem().toString());
        model.setMaxNakSentControl(maxNakSentControl);

        Model.MaxNoResponseControl maxNoResponseControl = new Model.MaxNoResponseControl();
        maxNoResponseControl.setEnabled(chkMaxNoResponse.isSelected());
        maxNoResponseControl.setValue(toLong(txtMaxNoResponse.getText().trim()));
        maxNoResponseControl.setRecourseAction(cmbMaxNoResponse.getSelectedItem().toString());
        model.setMaxNoResponseControl(maxNoResponseControl);

        Model.NakReceivedControl nakReceivedControl = new Model.NakReceivedControl();
        nakReceivedControl.setEnabled(chkNakReceived.isSelected());
        //nakReceivedControl.setValue(toLong(txtNakReceived.getText().toString()));
        nakReceivedControl.setRecourseAction(cmbNakReceived.getSelectedItem().toString());
        model.setNakReceivedControl(nakReceivedControl);
    }

    public HL7Error validateMe(boolean fireEvent) {
        HL7Error error = validateMaxNakSent(fireEvent);
        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
            error = validateMaxCanNakSent(fireEvent);
            if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                error = validateMaxConnectRetries(fireEvent);
                if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                    error = validateTimeToWait(fireEvent);
                    if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                        error = validateMaxNoResponse(fireEvent);
                        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                            error = validateNakReceived(fireEvent);
                            if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                                error = validateMaxNakReceived(fireEvent);
                            }
                        }
                    }
                }
            }
        }
        return error;
    }

    private HL7Error validateMaxNakSent(boolean fireEvent) {
        HL7Error error = new HL7Error();
        if (chkMaxNakSent.isSelected()) {

            if (cmbMaxNakSent.getSelectedItem().equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.SELECT_VALID_RECOURSE_ACTION"));
            } else {

                String value = txtMaxNakSent.getText().trim();

                try {
                    long l = Long.parseLong(value);
                    if (l < 0) {
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NEGATIVE_VALUE"));
                    }
                } catch (NumberFormatException numberFormatException) {
                    error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                    error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NOT_NUMBER"));
                }
            }
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
        return error;
    }

    private HL7Error validateMaxCanNakSent(boolean fireEvent) {
        HL7Error error = new HL7Error();
        if (chkMaxCanNakSent.isSelected()) {
            if (cmbMaxCanNakSent.getSelectedItem().equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.SELECT_VALID_RECOURSE_ACTION"));
            } else {

                String value = txtMaxCanNakSent.getText().trim();

                try {
                    long l = Long.parseLong(value);
                    if (l < 0) {
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NEGATIVE_VALUE"));
                    }
                } catch (NumberFormatException numberFormatException) {
                    error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                    error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NOT_NUMBER"));
                }
            }
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
        return error;

    }

    private HL7Error validateMaxConnectRetries(boolean fireEvent) {
        HL7Error error = new HL7Error();

        String connectRetries = txtMaxConnectRetries.getText().trim();
        if(chkMaxConnectRetries.isSelected()){
            if ( cmbMaxConnectRetries.getSelectedItem().equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.SELECT_VALID_RECOURSE_ACTION"));
            } else if (connectRetries.equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.INVALID_MAX_CONNECT_RETRIES_VALUE"));
            } else {

                String[] actions = connectRetries.split("\\s*,\\s*");

                Pattern sPattern = Pattern.compile(PATTERN);
                Matcher m;
                for (int i = 0; i < actions.length; i++) {
                    m = sPattern.matcher(actions[i]);
                    if (!m.matches()) {
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.INVALID_STRING_PATTERN"));
                        break;
                    }
                }
            }
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateMaxNakReceived(boolean fireEvent) {
        HL7Error error = new HL7Error();

        if (chkMaxNakReceived.isSelected()) {

            if (cmbMaxNakReceived.getSelectedItem().equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.SELECT_VALID_RECOURSE_ACTION"));
            } else {

                String value = txtMaxNakReceived.getText().trim();

                try {
                    long l = Long.parseLong(value);
                    if (l < 0) {
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NEGATIVE_VALUE"));
                    }
                } catch (NumberFormatException numberFormatException) {
                    error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                    error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NOT_NUMBER"));
                }
            }
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateMaxNoResponse(boolean fireEvent) {
        HL7Error error = new HL7Error();

        if (chkMaxNoResponse.isSelected()) {

            if (cmbMaxNoResponse.getSelectedItem().equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.SELECT_VALID_RECOURSE_ACTION"));
            } else {

                String value = txtMaxNoResponse.getText().trim();

                try {
                    long l = Long.parseLong(value);
                    if (l < 0) {
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NEGATIVE_VALUE"));
                    }
                } catch (NumberFormatException numberFormatException) {
                    error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                    error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NOT_NUMBER"));
                }
            }
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateNakReceived(boolean fireEvent) {
        HL7Error error = new HL7Error();
        if (chkNakReceived.isSelected()) {

            if (cmbNakReceived.getSelectedItem().equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.SELECT_VALID_RECOURSE_ACTION"));
            }
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateTimeToWait(boolean fireEvent) {
        HL7Error error = new HL7Error();
        if (chkTimeToWait.isSelected()) {

            if (cmbTimeToWait.getSelectedItem().equals("")) {
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.SELECT_VALID_RECOURSE_ACTION"));
            } else {

                String value = txtTimeToWait.getText().trim();

                try {
                    long l = Long.parseLong(value);
                    if (l < 0) {
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NEGATIVE_VALUE"));
                    }
                } catch (NumberFormatException numberFormatException) {
                    error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                    error.setErrorMessage(getValidationErrorMessage("HL7CommunicationControl.NOT_NUMBER"));
                }
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

    private Long toLong(String str) {
        try {
            return Long.valueOf(str);
        } catch (NumberFormatException e) {
            return 0L;
        }
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

    public static void syncCommControlPanel_ToFrom(Form.FormModel destModel, Form.FormModel srcModel)
            throws ModelModificationException {
        if (!(destModel instanceof CommunicationControlForm.Model)) {
            return;
        }
        if (!(srcModel instanceof CommunicationControlForm.Model)) {
            return;
        }
        CommunicationControlForm.Model dest =
                (CommunicationControlForm.Model) destModel;
        CommunicationControlForm.Model src =
                (CommunicationControlForm.Model) srcModel;

        dest.setMaxCannedNakSentControl(src.getMaxCannedNakSentControl());
        dest.setMaxConnectRetriesControl(src.getMaxConnectRetriesControl());
        dest.setMaxNakReceivedControl(src.getMaxNakReceivedControl());
        dest.setMaxNakSentControl(src.getMaxNakSentControl());
        dest.setMaxNoResponseControl(src.getMaxNoResponseControl());
        dest.setNakReceivedControl(src.getNakReceivedControl());
        dest.setTimeToWaitControl(src.getTimeToWaitControl());

    }

    /**
     * Data model that this view/panel can understand. Implement this interface
     * to supply this panel with content.
     */
    public interface Model extends FormModel {

        public abstract class CommunicationControl {

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getRecourseAction() {
                return recourseAction;
            }

            public void setRecourseAction(String recourseAction) {
                this.recourseAction = recourseAction;
            }

            public Long getValue() {
                return value;
            }

            public void setValue(Long value) {
                this.value = value;
            }
            private Long value = 0l;
            private boolean enabled;
            private String recourseAction = "";
        }

        public class TimeToWaitControl extends CommunicationControl {
        }

        public class NakReceivedControl extends CommunicationControl {
        }

        public class MaxNoResponseControl extends CommunicationControl {
        }

        public class MaxNakReceivedControl extends CommunicationControl {
        }

        public class MaxNakSentControl extends CommunicationControl {
        }

        public class MaxCannedNakSentControl extends CommunicationControl {
        }

        public class MaxConnectRetriesControl extends CommunicationControl {

            private String valueAsString = "";

            public String getValueAsString() {
                return this.valueAsString;
            }

            public void setValueAsString(String valueAsString) {
                this.valueAsString = valueAsString;
            }
        }

        public TimeToWaitControl getTimeToWaitControl();

        public void setTimeToWaitControl(TimeToWaitControl value);

        public NakReceivedControl getNakReceivedControl();

        public void setNakReceivedControl(NakReceivedControl value);

        public MaxNoResponseControl getMaxNoResponseControl();

        public void setMaxNoResponseControl(MaxNoResponseControl value);

        public MaxNakReceivedControl getMaxNakReceivedControl();

        public void setMaxNakReceivedControl(MaxNakReceivedControl value);

        public MaxNakSentControl getMaxNakSentControl();

        public void setMaxNakSentControl(MaxNakSentControl value);

        public MaxCannedNakSentControl getMaxCannedNakSentControl();

        public void setMaxCannedNakSentControl(MaxCannedNakSentControl value);

        public MaxConnectRetriesControl getMaxConnectRetriesControl();

        public void setMaxConnectRetriesControl(MaxConnectRetriesControl value);
    }
}
