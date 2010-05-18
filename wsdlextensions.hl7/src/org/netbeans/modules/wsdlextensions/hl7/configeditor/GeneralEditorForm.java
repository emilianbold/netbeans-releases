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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.GeneralPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.HL7BindingsConfigurationEditorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.wsdlextensions.hl7.*;

import static org.netbeans.modules.wsdlextensions.hl7.validator.AddressURL.*;


/**
 *
 * @author Vishnuvardhan P.R
 */
public class GeneralEditorForm extends GeneralPanel implements Form {

    private static Logger logger = Logger.getLogger(GeneralEditorForm.class.getName());
    private final Model model;
    private final String default_location = "hl7://localhost:4040";
    private final String default_encoder = "hl7encoder-1.0";
    private final String default_startBlockChar = "11";
    private final String default_endBlockChar = "13";
    private final String default_endDataChar = "28";
    private final String default_mllpv2RetryCount = "0";
    private final String default_mllpv2RetryInterval = "0";
    private final String default_mllpv2TimeToWaitAckNak = "0";
    
    private String templateConst;
    private static ResourceBundle validationErrMessages = ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.hl7.validator.Bundle");
    
    private HL7BindingsConfigurationEditorPanel holdingPanel = null;

    public GeneralEditorForm(Model model, String templateConst) {
        super();
        this.model = model;
        this.templateConst = templateConst;
        init();
        addEventListeners();

    /*SwingUtilities.invokeLater(new Runnable() {
    
    public void run() {
    //txtLocation.requestFocusInWindow();
    }
    });*/

    }
    
    public void setHoldingPanel(HL7BindingsConfigurationEditorPanel holdingPanel){
        this.holdingPanel = holdingPanel;
    }
    
    public void doFirePropertyChange(String propertyName,
                                      Object oldValue, Object newValue){
        if(this.holdingPanel == null){
            firePropertyChange(propertyName, oldValue, newValue);
        }else{
            this.holdingPanel.doFirePropertyChange(propertyName, oldValue, newValue);
        }
    }

    public HL7Error validateMe(boolean fireEvent) {
        HL7Error error = validateLocation(fireEvent);
        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
            error = validateStartBlkChar(fireEvent);

            if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                error = validateEndBlkChar(fireEvent);

                if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                    error = validateEndDataChar(fireEvent);

                    if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                        error = validateHLLPChecksum(fireEvent);

                        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
							error = validatePersistence(fireEvent);
							if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
								error = validateMLLPV2NoOfRetries(fireEvent);

                            if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                                error = validateMLLPV2RetriesInterval(fireEvent);

                                if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(error.getErrorMode())) {
                                    error = validateMLLPV2AckNakDuration(fireEvent);
									}
                                }
                            }
                        }
                    }
                }
            }
        }
        return error;
    }

    class MyDocumentListener implements DocumentListener {

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
    
    class MyFocusListener extends FocusAdapter{
        public void focusGained(FocusEvent e) {
            updateDescriptionArea(e);
        }
    }

    private void addEventListeners() {
        
        MyDocumentListener docListener = new MyDocumentListener();
        MyFocusListener focusListener = new MyFocusListener();
        
        cmbTransportProtocol.addFocusListener(focusListener);
        cmbLLPType.addFocusListener(focusListener);
        txtEncoder.addFocusListener(focusListener);
        
        txtLocation.getDocument().addDocumentListener(docListener);
        txtLocation.addFocusListener(focusListener);
        
        txtStartBlockChar.getDocument().addDocumentListener(docListener);
        txtStartBlockChar.addFocusListener(focusListener);
        
        txtEndBlockChar.getDocument().addDocumentListener(docListener);
        txtEndBlockChar.addFocusListener(focusListener);
        
        txtEndDataChar.getDocument().addDocumentListener(docListener);
        txtEndDataChar.addFocusListener(focusListener);
        
        txtMLLP2RetriesCount.getDocument().addDocumentListener(docListener);
        txtMLLP2RetriesCount.addFocusListener(focusListener);
        
        txtMLLPv2RetryInterval.getDocument().addDocumentListener(docListener);
        txtMLLPv2RetryInterval.addFocusListener(focusListener);
        
        txtMLLPV2DurationAckNak.getDocument().addDocumentListener(docListener);
        txtMLLPV2DurationAckNak.addFocusListener(focusListener);
        
        chkHLLPChecksum.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                validateMe(true);
            }
        });
        chkHLLPChecksum.addFocusListener(focusListener);
        
        cmbLLPType.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
				 if (e.getSource() == cmbLLPType) {
					if(cmbLLPType.getSelectedItem().toString().equals(HL7ProtocolProperties.LLPType.MLLPV1.getName())){
						if(chkHLLPChecksum.isSelected())
							chkHLLPChecksum.setSelected(false);
						chkHLLPChecksum.setEnabled(false);
						lblHLLPChecksum.setEnabled(false);
						chkPersistence.setEnabled(true);
						lblPersistence.setEnabled(true);
					}else if(cmbLLPType.getSelectedItem().toString().equals(HL7ProtocolProperties.LLPType.MLLPV2.getName())){
						if(chkHLLPChecksum.isSelected())
							chkHLLPChecksum.setSelected(false);
						chkHLLPChecksum.setEnabled(false);
						lblHLLPChecksum.setEnabled(false);
						if(chkPersistence.isSelected())
							chkPersistence.setSelected(false);
						chkPersistence.setEnabled(false);
						lblPersistence.setEnabled(false);
					}else{
						chkHLLPChecksum.setEnabled(true);
						lblHLLPChecksum.setEnabled(true);
						chkPersistence.setEnabled(true);
						lblPersistence.setEnabled(true);
					}
				 }
            }
        });
        
        chkPersistence.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                validateMe(true);
            }
        });
        chkPersistence.addFocusListener(focusListener);
        radUseEncoded.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(radUseEncoded.isSelected()){
                    txtEncoder.setText(default_encoder);
                }
            }
        });
        radUseEncoded.addFocusListener(focusListener);
        
        radUseLiteral.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(radUseLiteral.isSelected()){
                    txtEncoder.setText("");
                }
            }
        });        
        radUseLiteral.addFocusListener(focusListener);
    }
    
    private void updateDescriptionArea(FocusEvent evt){
        this.descriptionPanel.setText("");

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == cmbTransportProtocol) {
            desc = new String[]{"TransportProtocol\n\n",
                    cmbTransportProtocol.getToolTipText()};
            casaEdited = true;
        } else if(evt.getSource() == txtLocation){
            desc = new String[]{"Location\n\n",txtLocation.getToolTipText()};
        } else if(evt.getSource() == radUseLiteral){
            desc = new String[]{"Literal\n\n",radUseLiteral.getToolTipText()};
        } else if(evt.getSource() == radUseEncoded){
            desc = new String[]{"Encoded\n\n",radUseEncoded.getToolTipText()};
        } else if(evt.getSource() == txtEncoder){
            desc = new String[]{"Encoder\n\n",txtEncoder.getToolTipText()};
        } else if(evt.getSource() == cmbLLPType){
            desc = new String[]{"LLPType\n\n",cmbLLPType.getToolTipText()};
        } else if(evt.getSource() == txtStartBlockChar){
            desc = new String[]{"Start Block Char\n\n",txtStartBlockChar.getToolTipText()};
        } else if(evt.getSource() == txtEndBlockChar){
            desc = new String[]{"End Block Char\n\n",txtEndBlockChar.getToolTipText()};
        } else if(evt.getSource() == txtEndDataChar){
            desc = new String[]{"End Data Char\n\n",txtEndDataChar.getToolTipText()};
        } else if(evt.getSource() == chkHLLPChecksum){
            desc = new String[]{"HLLP Checksum\n\n",chkHLLPChecksum.getToolTipText()};
        } else if(evt.getSource() == chkPersistence){
            desc = new String[]{"Persistence\n\n",chkPersistence.getToolTipText()};
        } else if(evt.getSource() == txtMLLP2RetriesCount){
            desc = new String[]{"Retries count on Receipt of Nak\n\n",txtMLLP2RetriesCount.getToolTipText()};
        }else if(evt.getSource() == txtMLLPV2DurationAckNak){
            desc = new String[]{"Time To Wait for Ack / Nak\n\n",txtMLLPV2DurationAckNak.getToolTipText()};
        }else if(evt.getSource() == txtMLLPv2RetryInterval){
            desc = new String[]{"Retry Interval\n\n",txtMLLPv2RetryInterval.getToolTipText()};
        }
        
        if (desc != null) {
                // Insert the image
                if (casaEdited) {
                }
                this.descriptionPanel.setText(desc[0], desc[1]);
                return;
        }

    }

    private HL7Error validateLocation(boolean fireEvent) {

        HL7Error error = new HL7Error();
        String url = txtLocation.getText();


        try {
            // if missing
            if (url == null || url.trim().length() == 0) {
                error.setErrorMessage(getValidationErrorMessage("HL7Address.MISSING_HL7_URL"));
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                return error;
            }

            // if still the place holder
            if (url.startsWith(HL7_URL_PLACEHOLDER)) {
                error.setErrorMessage(getValidationErrorMessage("HL7Address.REPLACE_HL7_URL_PLACEHOLDER_WITH_REAL_URL"));
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                return error;
            }

            if (!url.startsWith(HL7_URL_PREFIX)) {
                error.setErrorMessage(getValidationErrorMessage("HL7Address.INVALID_HL7_URL_PREFIX") + url);
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                return error;
            }
            String scheme = "hl7";
            if (url.length() > HL7_URL_PREFIX.length()) {
                String rest = url.substring(HL7_URL_PREFIX.length());
                if (rest.indexOf(URL_PATH_DELIM) >= 0) {
                    error.setErrorMessage(getValidationErrorMessage("HL7Address.INVALID_HL7_URL_PATH_NOT_ALLOWED") + url);
                    error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                    return error;
                }

                int l = rest.trim().length();
                int i = 0;
                StringBuffer cur = new StringBuffer();
                int at = 0;
                int col = 0;
                List comps = new Vector();
                while (i < l) {
                    char c = rest.charAt(i);
                    switch (c) {
                        case '\\':
                            if (i + 1 < l) {
                                cur.append(url.charAt(i));
                                i = i + 2;
                            } else {
                                cur.append(c);
                                i++;
                            }
                            break;
                        case ':':
                            col++;
                            if (col > 1 || cur.length() == 0 /* :password and :port are invalid */) {
                                // in each part: either user:password
                                // or host:port, there can be at most 1
                                // ':' delimiter;
                                error.setErrorMessage(getValidationErrorMessage("HL7Address.MALFORMED_HL7_URL") + url);
                                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                                return error;
                            }
                            comps.add(cur.toString());
                            cur = new StringBuffer();
                            i++;
                            break;
                        default:
                            cur.append(c);
                            i++;
                    }

                }
                String tString = rest.trim();
                String port = tString.substring(tString.indexOf(URL_COLON_DELIM) + 1);
                comps.add(port);

                String host;
                switch (comps.size()) {
                    case 1:
                        host = (String) comps.get(0);
                        break;
                    case 2:
                        host = (String) comps.get(0);
                        port = (String) comps.get(1);
                        boolean goodPort = true;
                        if (port != null && port.trim().length() > 0) {
                            // must be a positive int
                            try {
                                int pt = Integer.parseInt(port);
                                if (pt <= 0) {
                                    goodPort = false;
                                }
                            } catch (Exception e) {
                                goodPort = false;
                            }
                        }

                        if (!goodPort) {
                            error.setErrorMessage(getValidationErrorMessage("HL7Address.INVALID_PORT_IN_URL") + url);
                            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                            return error;
                        }

                        break;
                    default:
                        error.setErrorMessage(getValidationErrorMessage("HL7Address.MALFORMED_HL7_URL") + url);
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        return error;
                }

                if (host == null || host.trim().length() == 0) {
                    error.setErrorMessage(getValidationErrorMessage("HL7Address.MALFORMED_HL7_URL_HOST_REQUIRED") + url);
                    error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                    return error;
                }
            } else {
                error.setErrorMessage(getValidationErrorMessage("HL7Address.MALFORMED_HL7_URL") + url);
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                return error;
            }


            return error;

        } finally {
            if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
                doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
            } else {
                doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
            }

        }
    }

    private HL7Error validateStartBlkChar(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String startBlkChar = txtStartBlockChar.getText();

        try {
            byte b = Byte.parseByte(startBlkChar);
        } catch (NumberFormatException numberFormatException) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_HL7_STARTBLOCK_CHARACTER"));
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateEndBlkChar(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String endBlkChar = txtEndBlockChar.getText();

        try {
            byte b = Byte.parseByte(endBlkChar);
        } catch (NumberFormatException numberFormatException) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_HL7_ENDBLOCK_CHARACTER"));
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateEndDataChar(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String endDataChar = txtEndDataChar.getText();

        try {
            byte b = Byte.parseByte(endDataChar);
        } catch (NumberFormatException numberFormatException) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_HL7_ENDDATA_CHARACTER"));
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateHLLPChecksum(boolean fireEvent) {
        HL7Error error = new HL7Error();

        if(chkHLLPChecksum.isSelected() && !cmbLLPType.getSelectedItem().toString().equals(HL7ProtocolProperties.LLPType.HLLP.getName())){
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.HLLP_CHECKSUM_ONLY_FOR_HLLP"));
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validatePersistence(boolean fireEvent) {
        HL7Error error = new HL7Error();

        if(chkPersistence.isSelected() && !cmbLLPType.getSelectedItem().toString().equals(HL7ProtocolProperties.LLPType.MLLPV1.getName())){
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.PERSISTENCE_ONLY_FOR_MLLPv1"));
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }
    private HL7Error validateMLLPV2NoOfRetries(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String retriesCount = txtMLLP2RetriesCount.getText();

        try {
            int i = Integer.parseInt(retriesCount);
        } catch (NumberFormatException numberFormatException) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_MLLPV2_RETRIES_COUNT"));
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateMLLPV2RetriesInterval(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String retriesCount = txtMLLP2RetriesCount.getText();

        try {
            int i = Integer.parseInt(retriesCount);
        } catch (NumberFormatException numberFormatException) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_MLLPV2_RETRY_INTERVAL"));
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private HL7Error validateMLLPV2AckNakDuration(boolean fireEvent) {
        HL7Error error = new HL7Error();
        String ackNakDuration = txtMLLPV2DurationAckNak.getText();

        try {
            int i = Integer.parseInt(ackNakDuration);
        } catch (NumberFormatException numberFormatException) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(getValidationErrorMessage("HL7ProtocolProperties.INVALID_MLLPV2_TIME_TO_WAIT"));
        }
        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }
    
    private String getValidationErrorMessage(String key){
        return getValidationErrorMessage(key, null);
    }
    private String getValidationErrorMessage(String key, Object[] params){
        String fmt = validationErrMessages.getString(key);
        if (params != null) {
            return MessageFormat.format(fmt, params);
        } else {
            return fmt;
        }
    }
        

    private void init() {
        if (HL7Constants.TEMPLATE_IN.equals(this.templateConst)) {
            mllpv2panel.setVisible(false);
        }

        radUseEncoded.setSelected(true);
        txtEncoder.setText(default_encoder);

        txtLocation.setText(default_location);
        DefaultComboBoxModel transportList = new DefaultComboBoxModel(
                new String[]{
                    HL7Address.TransportProtocol.TCPIP.getName()
                });
        transportList.setSelectedItem(HL7Address.TransportProtocol.TCPIP.getName());
        cmbTransportProtocol.setModel(transportList);

        DefaultComboBoxModel llpTypeList = new DefaultComboBoxModel(
                new String[]{
                    HL7ProtocolProperties.LLPType.HLLP.getName(),
                    HL7ProtocolProperties.LLPType.MLLPV1.getName(),
                    HL7ProtocolProperties.LLPType.MLLPV2.getName()
                });
        llpTypeList.setSelectedItem(HL7ProtocolProperties.LLPType.MLLPV1.getName());
        cmbLLPType.setModel(llpTypeList);

        txtStartBlockChar.setText(default_startBlockChar);
        txtEndBlockChar.setText(default_endBlockChar);
        txtEndDataChar.setText(default_endDataChar);
        chkHLLPChecksum.setSelected(false);
        chkHLLPChecksum.setEnabled(false);
		lblHLLPChecksum.setEnabled(false);
        chkPersistence.setSelected(false);
        txtMLLP2RetriesCount.setText(default_mllpv2RetryCount);
        txtMLLPv2RetryInterval.setText(default_mllpv2RetryInterval);
        txtMLLPV2DurationAckNak.setText(default_mllpv2TimeToWaitAckNak);

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

        DefaultComboBoxModel transportList = new DefaultComboBoxModel(
                new String[]{
                    HL7Address.TransportProtocol.TCPIP.getName()
                });
        transportList.setSelectedItem(model.getTransportProtocol());
        cmbTransportProtocol.setModel(transportList);
        txtLocation.setText(model.getLocation());
        DefaultComboBoxModel llpTypeList = new DefaultComboBoxModel(
                new String[]{
                    HL7ProtocolProperties.LLPType.HLLP.getName(),
                    HL7ProtocolProperties.LLPType.MLLPV1.getName(),
                    HL7ProtocolProperties.LLPType.MLLPV2.getName()
                });
        llpTypeList.setSelectedItem(model.getLLPType());
        cmbLLPType.setModel(llpTypeList);
        txtStartBlockChar.setText(model.getStartBlockCharacter() != null ? model.getStartBlockCharacter().toString() : "");
        txtEndBlockChar.setText(model.getEndBlockCharacter() != null ? model.getEndBlockCharacter().toString() : "");
        txtEndDataChar.setText(model.getEndDataCharacter() != null ? model.getEndDataCharacter().toString() : "");
        chkHLLPChecksum.setSelected(model.isHLLPChecksumEnabled());
        chkPersistence.setSelected(model.isPersistenceEnabled());
        txtMLLP2RetriesCount.setText(String.valueOf(model.getMllpv2RetriesCountOnNak()));
        txtMLLPv2RetryInterval.setText(String.valueOf(model.getMllpv2RetryInterval()));
        txtMLLPV2DurationAckNak.setText(String.valueOf(model.getMllpv2TimeToWaitForAckNak()));
        if (HL7Message.Use.LITERAL.getName().equals(model.getUse())) {
            radUseLiteral.setSelected(true);
        } else {
            radUseEncoded.setSelected(true);
        }
        txtEncoder.setText(model.getEncodingStyle());

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

        model.setTransportProtocol(cmbTransportProtocol.getSelectedItem().toString());
        model.setLocation(txtLocation.getText().trim());
        model.setLLPType(cmbLLPType.getSelectedItem().toString());
        try {
            model.setStartBlockCharacter(new Byte(txtStartBlockChar.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warning("Invalid Value provided for \"Start Block Character\"");
        }
        try {
            model.setEndBlockCharacter(new Byte(txtEndBlockChar.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warning("Invalid Value provided for \"End Block Character\"");
        }
        try {
            model.setEndDataCharacter(new Byte(txtEndDataChar.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warning("Invalid Value provided for \"End Data Character\"");
        }

        model.setHLLPChecksumEnabled(chkHLLPChecksum.isSelected());
        model.setPersistenceEnabled(chkPersistence.isSelected());
        model.setUse(radUseLiteral.isSelected() ? HL7Message.Use.LITERAL.getName() : HL7Message.Use.ENCODED.getName());
        model.setEncodingStyle(txtEncoder.getText().trim());
        try {
            model.setMllpv2RetriesCountOnNak(Integer.parseInt(txtMLLP2RetriesCount.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warning("Invalid Value provided for \"MLLPV2 Retries Count on Nak\"");
        }
        try {
            model.setMllpv2RetryInterval(Long.parseLong(txtMLLPv2RetryInterval.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warning("Invalid Value provided for \"MLLPV2 Retry Interval\"");
        }
        try {
            model.setMllpv2TimeToWaitForAckNak(Long.parseLong(txtMLLPV2DurationAckNak.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warning("Invalid Value provided for \"MLLPV2 Time to Wait for Ack/Nak\"");
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

    public static void syncGeneralPanel_ToFrom(Form.FormModel destModel, Form.FormModel srcModel)
            throws ModelModificationException {
        if (!(destModel instanceof GeneralEditorForm.Model)) {
            return;
        }
        if (!(srcModel instanceof GeneralEditorForm.Model)) {
            return;
        }
        GeneralEditorForm.Model dest =
                (GeneralEditorForm.Model) destModel;
        GeneralEditorForm.Model src =
                (GeneralEditorForm.Model) srcModel;

        dest.setTransportProtocol(src.getTransportProtocol());
        dest.setLocation(src.getLocation());
        dest.setLLPType(src.getLLPType());
        dest.setStartBlockCharacter(src.getStartBlockCharacter());
        dest.setEndBlockCharacter(src.getEndBlockCharacter());
        dest.setEndDataCharacter(src.getEndDataCharacter());
        dest.setHLLPChecksumEnabled(src.isHLLPChecksumEnabled());
        dest.setPersistenceEnabled(src.isPersistenceEnabled());
        //dest.setPart(src.getPart());
        dest.setUse(src.getUse());
        dest.setEncodingStyle(src.getEncodingStyle());
        dest.setMllpv2RetriesCountOnNak(src.getMllpv2RetriesCountOnNak());
        dest.setMllpv2RetryInterval(src.getMllpv2RetryInterval());
        dest.setMllpv2TimeToWaitForAckNak(src.getMllpv2TimeToWaitForAckNak());
    }

    /**
     * Data model that this view/panel can understand. Implement this interface
     * to supply this panel with content.
     */
    public interface Model extends FormModel {

        String getLocation();

        void setLocation(String location);

        String getTransportProtocol();

        void setTransportProtocol(String transportProtocol);

        String getUse();

        void setUse(String use);

        String getEncodingStyle();

        void setEncodingStyle(String encodingStyle);

        //String getPart();
        //void setPart(String part);
        String getLLPType();

        void setLLPType(String llpType);

        Byte getStartBlockCharacter();

        void setStartBlockCharacter(Byte startBlockChar);

        Byte getEndBlockCharacter();

        void setEndBlockCharacter(Byte endBlockChar);

        Byte getEndDataCharacter();

        void setEndDataCharacter(Byte endDataChar);

        boolean isHLLPChecksumEnabled();

        void setHLLPChecksumEnabled(boolean enabled);

        int getMllpv2RetriesCountOnNak();

        void setMllpv2RetriesCountOnNak(int count);

        long getMllpv2RetryInterval();

        void setMllpv2RetryInterval(long interval);

        long getMllpv2TimeToWaitForAckNak();

        void setMllpv2TimeToWaitForAckNak(long duration);
        boolean isPersistenceEnabled();

        void setPersistenceEnabled(boolean enabled);
    }
}
