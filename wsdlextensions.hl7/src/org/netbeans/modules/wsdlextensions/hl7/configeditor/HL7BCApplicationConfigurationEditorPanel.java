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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.xml.wsdl.bindingsupport.appconfig.spi.CompositeDataEditorPanel;

/**
 *
 * @author jfu
 */
public class HL7BCApplicationConfigurationEditorPanel
        extends CompositeDataEditorPanel implements DocumentListener, ActionListener {
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.hl7.configeditor.Bundle");

    private static final String NUM_PARM_NOT_IN_RANGE = "NUM_PARAM_NOT_IN_RANGE";
    private static final String PARAM_HAS_TOBE_INT = "PARAM_HAS_TOBE_INT";
    private static final String PARAMMSH_HAS_REQ_FIELDS = "PARAMMSH_HAS_REQ_FIELDS";
    private static final String PARAMSFT_HAS_REQ_FIELDS = "PARAMSFT_HAS_REQ_FIELDS";
    private static final String APP_CFG_NAME = "configurationName";
    public static final String APPLICATION_CONFIG_PROPERTY_HOSTNAME = "hostName";
    public static final String APPLICATION_CONFIG_PROPERTY_PORT = "port";
    public static final String APPLICATION_CONFIG_PROPERTY_VALIDATEMSH = "validateMSH";
    public static final String APPLICATION_CONFIG_PROPERTY_ACKNOWLEDGMENTMODE = "acknowledgmentMode";
    public static final String APPLICATION_CONFIG_PROPERTY_LLPTYPE = "llpType";
    public static final String APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR = "startBlockCharacter";
    public static final String APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR = "endBlockCharacter";
    public static final String APPLICATION_CONFIG_PROPERTY_ENDDATACHAR = "endDataCharacter";
    public static final String APPLICATION_CONFIG_PROPERTY_HLLPCHECKSUM = "hllpChecksumEnabled";
    public static final String APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT = "mllpv2RetriesCountOnNak";
    public static final String APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL = "mllpv2RetryInterval";
    public static final String APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK = "mllpv2TimeToWaitForAckNak";
    public static final String APPLICATION_CONFIG_PROPERTY_SEQUENCE_NUMBER_ENABLED = "seqNumEnabled";
    public static final String APPLICATION_CONFIG_PROPERTY_PROCESSING_ID = "processingID";
    public static final String APPLICATION_CONFIG_PROPERTY_VERSION_ID = "versionID";
    public static final String APPLICATION_CONFIG_PROPERTY_FIELD_SEPARATOR = "fieldSeparator";
    public static final String APPLICATION_CONFIG_PROPERTY_ENCODING_CHARS = "encodingCharacters";
    public static final String APPLICATION_CONFIG_PROPERTY_SENDING_APPLICATION = "sendingApplication";
    public static final String APPLICATION_CONFIG_PROPERTY_SENDING_FACILITY = "sendingFacility";
    public static final String APPLICATION_CONFIG_PROPERTY_SFT_ENABLED = "enabledSFT";
    public static final String APPLICATION_CONFIG_PROPERTY_SOFT_VENDOR_ORG = "softwareVendorOrganization";
    public static final String APPLICATION_CONFIG_PROPERTY_SOFT_CERTIFIED_VERSION = "softwareCertifiedVersionOrReleaseNumber";
    public static final String APPLICATION_CONFIG_PROPERTY_SOFT_PROD_NAME = "softwareProductName";
    public static final String APPLICATION_CONFIG_PROPERTY_SOFT_BINARY_ID = "softwareBinaryID";
    public static final String APPLICATION_CONFIG_PROPERTY_SOFT_PROD_INFO = "softwareProductInformation";
    public static final String APPLICATION_CONFIG_PROPERTY_SOFT_INSTALL_DATE = "softwareInstallDate";
    public static final String APPLICATION_CONFIG_PROPERTY_JOURNALLING_ENABLED = "journallingEnabled";

    public static final String HOSTNAME_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_HOSTNAME);
    public static final String PORT_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_PORT);
    public static final String VALIDATEMSH_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_VALIDATEMSH);
    public static final String ACKNOWLEDGMENTMODE_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_ACKNOWLEDGMENTMODE);
    public static final String LLPTYPE_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_LLPTYPE);
    public static final String STARTBLOCKCHAR_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR);
    public static final String ENDBLOCKCHAR_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR);
    public static final String ENDDATACHAR_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_ENDDATACHAR);
    public static final String HLLPCHECKSUM_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_HLLPCHECKSUM);
    public static final String MLLPV2_RETRIES_COUNT_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT);
    public static final String MLLPV2_RETRY_INTERVAL_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL);
    public static final String MLLPV2_TIME_TO_WAIT_FOR_ACKNAK_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK);
    public static final String SEQUENCE_NUMBER_ENABLED_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SEQUENCE_NUMBER_ENABLED);
    public static final String PROCESSING_ID_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_PROCESSING_ID);
    public static final String VERSION_ID_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_VERSION_ID);
    public static final String FIELD_SEPARATOR_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_FIELD_SEPARATOR);
    public static final String ENCODING_CHARS_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_ENCODING_CHARS);
    public static final String SENDING_APPLICATION_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SENDING_APPLICATION);
    public static final String SENDING_FACILITY_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SENDING_FACILITY);
    public static final String SFT_ENABLED_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SFT_ENABLED);
    public static final String SOFT_VENDOR_ORG_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_VENDOR_ORG);
    public static final String SOFT_CERTIFIED_VERSION_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_CERTIFIED_VERSION);
    public static final String SOFT_PROD_NAME_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_NAME);
    public static final String SOFT_BINARY_ID_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_BINARY_ID);
    public static final String SOFT_PROD_INFO_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_INFO);
    public static final String SOFT_INSTALL_DATE_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_INSTALL_DATE);
    public static final String JOURNALLINGENABLED_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_JOURNALLING_ENABLED);

    private static final String[] AppConfigRowAttrNames =
    {
    	APP_CFG_NAME,
    	APPLICATION_CONFIG_PROPERTY_HOSTNAME,
    	APPLICATION_CONFIG_PROPERTY_PORT,
    	APPLICATION_CONFIG_PROPERTY_VALIDATEMSH,
    	APPLICATION_CONFIG_PROPERTY_ACKNOWLEDGMENTMODE,
    	APPLICATION_CONFIG_PROPERTY_LLPTYPE,
    	APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR,
        APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR,
        APPLICATION_CONFIG_PROPERTY_ENDDATACHAR,
		APPLICATION_CONFIG_PROPERTY_HLLPCHECKSUM,
		APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT,
		APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL,
		APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK,
		APPLICATION_CONFIG_PROPERTY_SEQUENCE_NUMBER_ENABLED,
		APPLICATION_CONFIG_PROPERTY_PROCESSING_ID,
		APPLICATION_CONFIG_PROPERTY_VERSION_ID,
		APPLICATION_CONFIG_PROPERTY_FIELD_SEPARATOR,
		APPLICATION_CONFIG_PROPERTY_ENCODING_CHARS,
		APPLICATION_CONFIG_PROPERTY_SENDING_APPLICATION,
		APPLICATION_CONFIG_PROPERTY_SENDING_FACILITY,
		APPLICATION_CONFIG_PROPERTY_SFT_ENABLED,
		APPLICATION_CONFIG_PROPERTY_SOFT_VENDOR_ORG,
		APPLICATION_CONFIG_PROPERTY_SOFT_CERTIFIED_VERSION,
		APPLICATION_CONFIG_PROPERTY_SOFT_PROD_NAME,
		APPLICATION_CONFIG_PROPERTY_SOFT_BINARY_ID,
		APPLICATION_CONFIG_PROPERTY_SOFT_PROD_INFO,
		APPLICATION_CONFIG_PROPERTY_SOFT_INSTALL_DATE,
        APPLICATION_CONFIG_PROPERTY_JOURNALLING_ENABLED
    };
    private static final String[] AppConfigAttrDesc =
    {
    	"Application Configuration Name",
    	HOSTNAME_DESC,
    	PORT_DESC,
    	VALIDATEMSH_DESC,
    	ACKNOWLEDGMENTMODE_DESC,
    	LLPTYPE_DESC,
    	STARTBLOCKCHAR_DESC,
        ENDBLOCKCHAR_DESC,
		ENDDATACHAR_DESC,
		HLLPCHECKSUM_DESC,
		MLLPV2_RETRIES_COUNT_DESC,
		MLLPV2_RETRY_INTERVAL_DESC,
		MLLPV2_TIME_TO_WAIT_FOR_ACKNAK_DESC,
		SEQUENCE_NUMBER_ENABLED_DESC,
		PROCESSING_ID_DESC,
		VERSION_ID_DESC,
		FIELD_SEPARATOR_DESC,
		ENCODING_CHARS_DESC,
		SENDING_APPLICATION_DESC,
		SENDING_FACILITY_DESC,
		SFT_ENABLED_DESC,
		SOFT_VENDOR_ORG_DESC,
		SOFT_CERTIFIED_VERSION_DESC,
		SOFT_PROD_NAME_DESC,
		SOFT_BINARY_ID_DESC,
		SOFT_PROD_INFO_DESC,
		SOFT_INSTALL_DATE_DESC,
        JOURNALLINGENABLED_DESC
    };
    private static final OpenType[] AppConfigAttrTypes =
    {
    	SimpleType.STRING,
    	SimpleType.STRING,
		SimpleType.INTEGER,
    	SimpleType.BOOLEAN,
    	SimpleType.STRING,
    	SimpleType.STRING,
		SimpleType.INTEGER,
        SimpleType.INTEGER,
        SimpleType.INTEGER,
        SimpleType.BOOLEAN,
        SimpleType.INTEGER,
        SimpleType.INTEGER,
        SimpleType.INTEGER,
        SimpleType.BOOLEAN,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
        SimpleType.STRING,
        SimpleType.STRING,
        SimpleType.STRING,
        SimpleType.BOOLEAN,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.BOOLEAN
    };

    private static final String PARAM_NOT_SPECIFIED = "PARAM_NOT_SPECIFIED";

	private JLabel lblHostName;
	private JLabel lblPort;
	private JLabel lblValidateMSH;
	private JLabel lblAckMode;
	private JLabel lblLLPType;
	private JLabel lblStartBlockChar;
	private JLabel lblEndBlockChar;
	private JLabel lblEndDataChar;
	private JLabel lblHLLPCheckSum;
	private JLabel lblMLLPv2Retries;
	private JLabel lblMLLPv2RetryInterval;
	private JLabel lblMLLPv2TimeToWait;
	private JLabel lblSeqNumEnabled;
	private JLabel lblProcessingID;
	private JLabel lblVersionID;
	private JLabel lblFieldSep;
	private JLabel lblEncodingChar;
	private JLabel lblSendingApp;
	private JLabel lblSendingFacility;
	private JLabel lblSFTEnabled;
	private JLabel lblSoftVendor;
	private JLabel lblSoftCertified;
	private JLabel lblSoftProdName;
	private JLabel lblSoftBinaryID;
	private JLabel lblSoftProdInfo;
	private JLabel lblSoftInstallDate;
	private JLabel lblJournallingEnabled;
    
    private JTextField hostName;
    private JTextField port;
    private JCheckBox validateMSH;
    private JComboBox ackMode;
    private JComboBox llpType;
    private JTextField startBlockChar;
    private JTextField endBlockChar;
    private JTextField endDataChar;
    private JCheckBox hllpCheckSum;
    private JTextField mllpv2Retries;
    private JTextField mllpv2RetryInterval;
    private JTextField mllpv2TimeToWait;
    private JCheckBox seqNumEnabled;
    private JComboBox processingID;
    private JComboBox versionID;
    private JTextField fieldSep;
    private JTextField encodingChar;
    private JTextField sendingApp;
    private JTextField sendingFacility;
    private JCheckBox sftEnabled;
    private JTextField softVendor;
    private JTextField softCertified;
    private JTextField softProdName;
    private JTextField softBinaryID;
    private JTextField softProdInfo;
    private JTextField softInstallDate;
    private JCheckBox journallingEnabled;
    
    private String appConfigName;
    
    public HL7BCApplicationConfigurationEditorPanel(String appConfigName,
            CompositeData compositeData) {
        this.appConfigName = appConfigName;
        init(compositeData);
    }

    private String getDisplayName(String key) {
        return mMessages.getString(key);
    }
    
    private String getLocalizedMessage(String key, Object[] params) {
        String msg = null;
        String fmt = mMessages.getString(key);
        if ( fmt != null ) {
            if ( params != null ) {
                msg = MessageFormat.format(fmt, params);
            } else {
                msg = fmt;
            }
        }
        else {
            msg = key;
        }
        return msg;
    }
    
    private void init(CompositeData compositeData) {
        setLayout(new GridLayout(10, 6, 20,10));		

        hostName = new JTextField();
		//lblHostName = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_HOSTNAME));
		lblHostName = new JLabel();
        hostName.setText(compositeData == null ? "localhost" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_HOSTNAME));
		hostName.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.hostName.toolTipText")); // NOI18N
        lblHostName.setLabelFor(hostName);
		hostName.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.hostName.toolTipText")); // NOI18N
		hostName.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.hostName.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblHostName, mMessages.getString(APPLICATION_CONFIG_PROPERTY_HOSTNAME)); // NOI18N

        port = new JTextField();
		//lblPort = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_PORT));
		lblPort = new JLabel();
        port.setText(compositeData == null ? "4040" : compositeData.get(APPLICATION_CONFIG_PROPERTY_PORT).toString());
		port.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.port.toolTipText")); // NOI18N
        lblPort.setLabelFor(port);
		port.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.port.toolTipText")); // NOI18N
		port.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.port.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblPort, mMessages.getString(APPLICATION_CONFIG_PROPERTY_PORT)); // NOI18N

        validateMSH = new JCheckBox();
		//lblValidateMSH = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_VALIDATEMSH));
		lblValidateMSH = new JLabel();
        Boolean validate = (compositeData == null ? new Boolean(false) : (Boolean)compositeData.get(APPLICATION_CONFIG_PROPERTY_VALIDATEMSH));
        validateMSH.setSelected(validate.booleanValue());		
		validateMSH.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.validateMSH.toolTipText")); // NOI18N
        lblValidateMSH.setLabelFor(validateMSH);
		validateMSH.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.validateMSH.toolTipText")); // NOI18N
		validateMSH.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.validateMSH.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblValidateMSH, mMessages.getString(APPLICATION_CONFIG_PROPERTY_VALIDATEMSH)); // NOI18N

        ackMode = new JComboBox(
                new Object[] {
                    "",
                    "original",
                    "enhanced"
        });
		//lblAckMode = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_ACKNOWLEDGMENTMODE));
		lblAckMode = new JLabel();						
		ackMode.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.ackMode.toolTipText")); // NOI18N   
        lblAckMode.setLabelFor(ackMode);     
        String mode = compositeData == null ? "" : (String)compositeData.get(APPLICATION_CONFIG_PROPERTY_ACKNOWLEDGMENTMODE);
        ackMode.setSelectedItem(mode);
		ackMode.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.ackMode.toolTipText")); // NOI18N
		ackMode.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.ackMode.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblAckMode, mMessages.getString(APPLICATION_CONFIG_PROPERTY_ACKNOWLEDGMENTMODE)); // NOI18N

        llpType = new JComboBox(
                new Object[] {
                    "",
                    "MLLPv1",
                    "MLLPv2",
                    "HLLP"
        });
        //lblLLPType = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_LLPTYPE));
        lblLLPType = new JLabel();
        String type = compositeData == null ? "" : (String)compositeData.get(APPLICATION_CONFIG_PROPERTY_LLPTYPE);				
		llpType.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.llpType.toolTipText")); // NOI18N  
        lblLLPType.setLabelFor(llpType);     
        llpType.setSelectedItem(type);
		llpType.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.llpType.toolTipText")); // NOI18N
		llpType.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.llpType.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblLLPType, mMessages.getString(APPLICATION_CONFIG_PROPERTY_LLPTYPE)); // NOI18N

        startBlockChar = new JTextField();
		//lblStartBlockChar = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR));
		lblStartBlockChar = new JLabel();
        startBlockChar.setText(compositeData == null ? "11" : compositeData.get(APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR).toString());			
		startBlockChar.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.startBlockChar.toolTipText")); // NOI18N 
        lblStartBlockChar.setLabelFor(startBlockChar);
		startBlockChar.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.startBlockChar.toolTipText")); // NOI18N
		startBlockChar.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.startBlockChar.toolTipText")); // NOI18N 
		org.openide.awt.Mnemonics.setLocalizedText(lblStartBlockChar, mMessages.getString(APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR)); // NOI18N

        endBlockChar = new JTextField();
		//lblEndBlockChar = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR));
		lblEndBlockChar = new JLabel();
        endBlockChar.setText(compositeData == null ? "13" : compositeData.get(APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR).toString());			
		endBlockChar.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.endBlockChar.toolTipText")); // NOI18N
        lblEndBlockChar.setLabelFor(endBlockChar);
		endBlockChar.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.endBlockChar.toolTipText")); // NOI18N
		endBlockChar.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.endBlockChar.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblEndBlockChar, mMessages.getString(APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR)); // NOI18N

        endDataChar = new JTextField();
		//lblEndDataChar = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_ENDDATACHAR));
		lblEndDataChar = new JLabel();
        endDataChar.setText(compositeData == null ? "28" : compositeData.get(APPLICATION_CONFIG_PROPERTY_ENDDATACHAR).toString());			
		endDataChar.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.endDataChar.toolTipText")); // NOI18N
        lblEndDataChar.setLabelFor(endDataChar);
		endDataChar.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.endDataChar.toolTipText")); // NOI18N
		endDataChar.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.endDataChar.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblEndDataChar, mMessages.getString(APPLICATION_CONFIG_PROPERTY_ENDDATACHAR)); // NOI18N
        
        hllpCheckSum = new JCheckBox();
		//lblHLLPCheckSum = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_HLLPCHECKSUM));
		lblHLLPCheckSum = new JLabel();
        Boolean hllpchkEnabled = (compositeData == null ? new Boolean(false) : (Boolean)compositeData.get(APPLICATION_CONFIG_PROPERTY_HLLPCHECKSUM));
        hllpCheckSum.setSelected(hllpchkEnabled.booleanValue());			
		hllpCheckSum.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.hllpCheckSum.toolTipText")); // NOI18N
        lblHLLPCheckSum.setLabelFor(hllpCheckSum);
		hllpCheckSum.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.hllpCheckSum.toolTipText")); // NOI18N
		hllpCheckSum.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.hllpCheckSum.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblHLLPCheckSum, mMessages.getString(APPLICATION_CONFIG_PROPERTY_HLLPCHECKSUM)); // NOI18N

        String mllpv2RetriesValue = compositeData == null ? "0" : compositeData.get(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT).toString();
        mllpv2Retries = new JTextField();
		//lblMLLPv2Retries = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT));
		lblMLLPv2Retries = new JLabel();
        mllpv2Retries.setText(mllpv2RetriesValue);					
		mllpv2Retries.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2Retries.toolTipText")); // NOI18N
        lblMLLPv2Retries.setLabelFor(mllpv2Retries);
		mllpv2Retries.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2Retries.toolTipText")); // NOI18N
		mllpv2Retries.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2Retries.toolTipText")); // NOI18N 
		org.openide.awt.Mnemonics.setLocalizedText(lblMLLPv2Retries, mMessages.getString(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT)); // NOI18N

        String mllpv2Retry = compositeData == null ? "0" : compositeData.get(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL).toString();
        mllpv2RetryInterval = new JTextField();
		//lblMLLPv2RetryInterval = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL));
		lblMLLPv2RetryInterval = new JLabel();
        mllpv2RetryInterval.setText(mllpv2Retry);					
		mllpv2RetryInterval.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2RetryInterval.toolTipText")); // NOI18N
        lblMLLPv2RetryInterval.setLabelFor(mllpv2RetryInterval);
		mllpv2RetryInterval.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2RetryInterval.toolTipText")); // NOI18N
		mllpv2RetryInterval.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2RetryInterval.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblMLLPv2RetryInterval, mMessages.getString(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL)); // NOI18N

        String mllpv2TimeToWaitValue = compositeData == null ? "0" : compositeData.get(APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK).toString();
        mllpv2TimeToWait = new JTextField();
		//lblMLLPv2TimeToWait = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK));
		lblMLLPv2TimeToWait = new JLabel();
        mllpv2TimeToWait.setText(mllpv2TimeToWaitValue);					
		mllpv2TimeToWait.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2TimeToWait.toolTipText")); // NOI18N
        lblMLLPv2TimeToWait.setLabelFor(mllpv2TimeToWait);
		mllpv2TimeToWait.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2TimeToWait.toolTipText")); // NOI18N
		mllpv2TimeToWait.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.mllpv2TimeToWait.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblMLLPv2TimeToWait, mMessages.getString(APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK)); // NOI18N    
        
        seqNumEnabled = new JCheckBox();
		//lblSeqNumEnabled = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SEQUENCE_NUMBER_ENABLED));
		lblSeqNumEnabled = new JLabel();
        Boolean seqNumEnabledValue = (compositeData == null ? new Boolean(false) : (Boolean)compositeData.get(APPLICATION_CONFIG_PROPERTY_SEQUENCE_NUMBER_ENABLED));
        seqNumEnabled.setSelected(seqNumEnabledValue.booleanValue());					
		seqNumEnabled.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.seqNumEnabled.toolTipText")); // NOI18N
        lblSeqNumEnabled.setLabelFor(seqNumEnabled);
		seqNumEnabled.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.seqNumEnabled.toolTipText")); // NOI18N
		seqNumEnabled.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.seqNumEnabled.toolTipText")); // NOI18N 
		org.openide.awt.Mnemonics.setLocalizedText(lblSeqNumEnabled, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SEQUENCE_NUMBER_ENABLED)); // NOI18N    

        processingID = new JComboBox(
                new Object[] {
                    "",
                    "P",
                    "D",
                    "T"
        });
		//lblProcessingID = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_PROCESSING_ID));
		lblProcessingID = new JLabel();                
        String pid = compositeData == null ? "" : (String)compositeData.get(APPLICATION_CONFIG_PROPERTY_PROCESSING_ID);
        processingID.setSelectedItem(pid);					
		processingID.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.processingID.toolTipText")); // NOI18N
        lblProcessingID.setLabelFor(processingID);
		processingID.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.processingID.toolTipText")); // NOI18N
		processingID.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.processingID.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblProcessingID, mMessages.getString(APPLICATION_CONFIG_PROPERTY_PROCESSING_ID)); // NOI18N    

        versionID = new JComboBox(
                new Object[] {
                    "",
                    "2.1",
                    "2.2",
                    "2.3",
                    "2.3.1",
                    "2.4",
                    "2.5",
                    "2.5.1",
                    "2.6"
        });
        //lblVersionID = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_VERSION_ID));
        lblVersionID = new JLabel();
        String vid = compositeData == null ? "" : (String)compositeData.get(APPLICATION_CONFIG_PROPERTY_VERSION_ID);
        versionID.setSelectedItem(vid);				
		versionID.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.versionID.toolTipText")); // NOI18N
        lblVersionID.setLabelFor(versionID);
		versionID.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.versionID.toolTipText")); // NOI18N
		versionID.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.versionID.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblVersionID, mMessages.getString(APPLICATION_CONFIG_PROPERTY_VERSION_ID)); // NOI18N    
        
        fieldSep = new JTextField();
		//lblFieldSep = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_FIELD_SEPARATOR));
		lblFieldSep = new JLabel();
        fieldSep.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_FIELD_SEPARATOR));				
		fieldSep.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.fieldSep.toolTipText")); // NOI18N
        lblFieldSep.setLabelFor(fieldSep);
		fieldSep.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.fieldSep.toolTipText")); // NOI18N
		fieldSep.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.fieldSep.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblFieldSep, mMessages.getString(APPLICATION_CONFIG_PROPERTY_FIELD_SEPARATOR)); // NOI18N      
        
        encodingChar = new JTextField();
		//lblEncodingChar = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_ENCODING_CHARS));
		lblEncodingChar = new JLabel();
        encodingChar.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_ENCODING_CHARS));				
		encodingChar.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.encodingChar.toolTipText")); // NOI18N
        lblEncodingChar.setLabelFor(encodingChar);
		encodingChar.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.encodingChar.toolTipText")); // NOI18N
		encodingChar.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.encodingChar.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblEncodingChar, mMessages.getString(APPLICATION_CONFIG_PROPERTY_ENCODING_CHARS)); // NOI18N      
        
        sendingApp = new JTextField();
		//lblSendingApp = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SENDING_APPLICATION));
		lblSendingApp = new JLabel();
        sendingApp.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SENDING_APPLICATION));				
		sendingApp.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sendingApp.toolTipText")); // NOI18N
        lblSendingApp.setLabelFor(sendingApp);
		sendingApp.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sendingApp.toolTipText")); // NOI18N
		sendingApp.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sendingApp.toolTipText")); // NOI18N 
		org.openide.awt.Mnemonics.setLocalizedText(lblSendingApp, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SENDING_APPLICATION)); // NOI18N      
        
        sendingFacility = new JTextField();
		//lblSendingFacility = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SENDING_FACILITY));
		lblSendingFacility = new JLabel();
        sendingFacility.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SENDING_FACILITY));				
		sendingFacility.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sendingFacility.toolTipText")); // NOI18N
        lblSendingFacility.setLabelFor(sendingFacility);
		sendingFacility.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sendingFacility.toolTipText")); // NOI18N
		sendingFacility.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sendingFacility.toolTipText")); // NOI18N    
		org.openide.awt.Mnemonics.setLocalizedText(lblSendingFacility, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SENDING_FACILITY)); // NOI18N
        
        sftEnabled = new JCheckBox();
		//lblSFTEnabled = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SFT_ENABLED));
		lblSFTEnabled = new JLabel();
        Boolean sftEnabledValue = (compositeData == null ? new Boolean(false) : (Boolean)compositeData.get(APPLICATION_CONFIG_PROPERTY_SFT_ENABLED));
        sftEnabled.setSelected(sftEnabledValue.booleanValue());				
		sftEnabled.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sftEnabled.toolTipText")); // NOI18N
        lblSFTEnabled.setLabelFor(sftEnabled);
		sftEnabled.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sftEnabled.toolTipText")); // NOI18N
		sftEnabled.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.sftEnabled.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblSFTEnabled, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SFT_ENABLED)); // NOI18N
        
        softVendor = new JTextField();
		//lblSoftVendor = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SOFT_VENDOR_ORG));
		lblSoftVendor = new JLabel();
        softVendor.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SOFT_VENDOR_ORG));			
		softVendor.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softVendor.toolTipText")); // NOI18N
        lblSoftVendor.setLabelFor(softVendor);
		softVendor.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softVendor.toolTipText")); // NOI18N
		softVendor.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softVendor.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblSoftVendor, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_VENDOR_ORG)); // NOI18N
        
        softCertified = new JTextField();
		//lblSoftCertified = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SOFT_CERTIFIED_VERSION));
		lblSoftCertified = new JLabel();
        softCertified.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SOFT_CERTIFIED_VERSION));			
		softCertified.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softCertified.toolTipText")); // NOI18N
        lblSoftCertified.setLabelFor(softCertified);
		softCertified.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softCertified.toolTipText")); // NOI18N
		softCertified.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softCertified.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblSoftCertified, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_CERTIFIED_VERSION)); // NOI18N
        
        softProdName = new JTextField();
		//lblSoftProdName = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_NAME));
		lblSoftProdName = new JLabel();
        softProdName.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_NAME));		
		softProdName.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softProdName.toolTipText")); // NOI18N
        lblSoftProdName.setLabelFor(softProdName);
		softProdName.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softProdName.toolTipText")); // NOI18N
		softProdName.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softProdName.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblSoftProdName, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_NAME)); // NOI18N
        
        softBinaryID = new JTextField();
		//lblSoftBinaryID = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SOFT_BINARY_ID));
		lblSoftBinaryID = new JLabel();
        softBinaryID.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SOFT_BINARY_ID));		
		softBinaryID.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softBinaryID.toolTipText")); // NOI18N
        lblSoftBinaryID.setLabelFor(softBinaryID);
		softBinaryID.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softBinaryID.toolTipText")); // NOI18N
		softBinaryID.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softBinaryID.toolTipText")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblSoftBinaryID, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_BINARY_ID)); // NOI18N
        
        softProdInfo = new JTextField();
		//lblSoftProdInfo = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_INFO));
		lblSoftProdInfo = new JLabel();
        softProdInfo.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_INFO));		
		softProdInfo.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softProdInfo.toolTipText")); // NOI18N
        lblSoftProdInfo.setLabelFor(softProdInfo);
		softProdInfo.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softProdInfo.toolTipText")); // NOI18N
		softProdInfo.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softProdInfo.toolTipText")); // NOI18N 
		org.openide.awt.Mnemonics.setLocalizedText(lblSoftProdInfo, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_INFO)); // NOI18N
        
        softInstallDate = new JTextField();
		//lblSoftInstallDate = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SOFT_INSTALL_DATE));
		lblSoftInstallDate = new JLabel();
        softInstallDate.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_SOFT_INSTALL_DATE));		
		softInstallDate.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softInstallDate.toolTipText")); // NOI18N
        lblSoftInstallDate.setLabelFor(softInstallDate);
		softInstallDate.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softInstallDate.toolTipText")); // NOI18N
		softInstallDate.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.softInstallDate.toolTipText")); // NOI18N   
		org.openide.awt.Mnemonics.setLocalizedText(lblSoftInstallDate, mMessages.getString(APPLICATION_CONFIG_PROPERTY_SOFT_INSTALL_DATE)); // NOI18N

        journallingEnabled = new JCheckBox();
		//lblJournallingEnabled = new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_JOURNALLING_ENABLED));
		lblJournallingEnabled = new JLabel();
        Boolean journalling = (compositeData == null ? new Boolean(false) : (Boolean)compositeData.get(APPLICATION_CONFIG_PROPERTY_JOURNALLING_ENABLED));
        journallingEnabled.setSelected(journalling.booleanValue());		
		journallingEnabled.setToolTipText(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.journallingEnabled.toolTipText")); // NOI18N
        lblJournallingEnabled.setLabelFor(journallingEnabled);
		journallingEnabled.getAccessibleContext().setAccessibleName(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.journallingEnabled.toolTipText")); // NOI18N
		journallingEnabled.getAccessibleContext().setAccessibleDescription(mMessages.getString("HL7BCApplicationConfigurationEditorPanel.journallingEnabled.toolTipText")); // NOI18N  
		org.openide.awt.Mnemonics.setLocalizedText(lblJournallingEnabled, mMessages.getString(APPLICATION_CONFIG_PROPERTY_JOURNALLING_ENABLED)); // NOI18N 
        
        add(lblHostName);
        add(hostName);
        add(lblPort);
        add(port);
        add(lblValidateMSH);
        add(validateMSH);
        add(lblAckMode);
        add(ackMode);
        add(lblLLPType);
        add(llpType);
        add(lblStartBlockChar);
        add(startBlockChar);
        add(lblEndBlockChar);
        add(endBlockChar);
        add(lblEndDataChar);
        add(endDataChar);
        add(lblHLLPCheckSum);
        add(hllpCheckSum);
        add(lblMLLPv2Retries);
        add(mllpv2Retries);
        add(lblMLLPv2RetryInterval);
        add(mllpv2RetryInterval);
        add(lblMLLPv2TimeToWait);
        add(mllpv2TimeToWait);
        add(lblSeqNumEnabled);
        add(seqNumEnabled);
        add(lblProcessingID);
        add(processingID);
        add(lblVersionID);
        add(versionID);
        add(lblFieldSep);
        add(fieldSep);
        add(lblEncodingChar);
        add(encodingChar);
        add(lblSendingApp);
        add(sendingApp);
        add(lblSendingFacility);
        add(sendingFacility);
        add(lblSFTEnabled);
        add(sftEnabled);
        add(lblSoftVendor);
        add(softVendor);
        add(lblSoftCertified);
        add(softCertified);
        add(lblSoftProdName);
        add(softProdName);
        add(lblSoftBinaryID);
        add(softBinaryID);
        add(lblSoftProdInfo);
        add(softProdInfo);
        add(lblSoftInstallDate);
        add(softInstallDate);
        add(lblJournallingEnabled);
        add(journallingEnabled);

        hostName.getDocument().addDocumentListener(this);
        port.getDocument().addDocumentListener(this);
        startBlockChar.getDocument().addDocumentListener(this);
        endBlockChar.getDocument().addDocumentListener(this);
        endDataChar.getDocument().addDocumentListener(this);
        mllpv2Retries.getDocument().addDocumentListener(this);
        mllpv2RetryInterval.getDocument().addDocumentListener(this);
        mllpv2TimeToWait.getDocument().addDocumentListener(this);
        fieldSep.getDocument().addDocumentListener(this);
        encodingChar.getDocument().addDocumentListener(this);
        sendingApp.getDocument().addDocumentListener(this);
        sendingFacility.getDocument().addDocumentListener(this);
        softVendor.getDocument().addDocumentListener(this);
        softCertified.getDocument().addDocumentListener(this);
        softProdName.getDocument().addDocumentListener(this);
        softBinaryID.getDocument().addDocumentListener(this);
        softProdInfo.getDocument().addDocumentListener(this);
        softInstallDate.getDocument().addDocumentListener(this);
        
        ackMode.addActionListener(this);        
        llpType.addActionListener(this);        
        processingID.addActionListener(this);        
        versionID.addActionListener(this);
        validateMSH.addActionListener(this);
        sftEnabled.addActionListener(this);
        journallingEnabled.addActionListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                validateContent();
            }
        });
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    }

    public CompositeData getCompositeData() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(APP_CFG_NAME, appConfigName);
        map.put(APPLICATION_CONFIG_PROPERTY_HOSTNAME, hostName.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_PORT, Integer.parseInt(port.getText()));
        map.put(APPLICATION_CONFIG_PROPERTY_VALIDATEMSH, validateMSH.isSelected());
        map.put(APPLICATION_CONFIG_PROPERTY_ACKNOWLEDGMENTMODE, ackMode.getSelectedItem() != null ?  ackMode.getSelectedItem().toString() : "");
        map.put(APPLICATION_CONFIG_PROPERTY_LLPTYPE, llpType.getSelectedItem() != null ?  llpType.getSelectedItem().toString() : "");
        map.put(APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR, Integer.parseInt(startBlockChar.getText()));
        map.put(APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR, Integer.parseInt(endBlockChar.getText()));
        map.put(APPLICATION_CONFIG_PROPERTY_ENDDATACHAR, Integer.parseInt(endDataChar.getText()));
        map.put(APPLICATION_CONFIG_PROPERTY_HLLPCHECKSUM, hllpCheckSum.isSelected());
        map.put(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT, Integer.parseInt(mllpv2Retries.getText()));
        map.put(APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL, Integer.parseInt(mllpv2RetryInterval.getText()));
        map.put(APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK, Integer.parseInt(mllpv2TimeToWait.getText()));
        map.put(APPLICATION_CONFIG_PROPERTY_SEQUENCE_NUMBER_ENABLED, seqNumEnabled.isSelected());
        map.put(APPLICATION_CONFIG_PROPERTY_PROCESSING_ID, processingID.getSelectedItem() != null ?  processingID.getSelectedItem().toString() : "");
        map.put(APPLICATION_CONFIG_PROPERTY_VERSION_ID, versionID.getSelectedItem() != null ?  versionID.getSelectedItem().toString() : "");
        map.put(APPLICATION_CONFIG_PROPERTY_FIELD_SEPARATOR, fieldSep.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_ENCODING_CHARS, encodingChar.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SENDING_APPLICATION, sendingApp.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SENDING_FACILITY, sendingFacility.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SFT_ENABLED, sftEnabled.isSelected());
        map.put(APPLICATION_CONFIG_PROPERTY_SOFT_VENDOR_ORG, softVendor.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SOFT_CERTIFIED_VERSION, softCertified.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_NAME, softProdName.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SOFT_BINARY_ID, softBinaryID.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SOFT_PROD_INFO, softProdInfo.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SOFT_INSTALL_DATE, softInstallDate.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_JOURNALLING_ENABLED, journallingEnabled.isSelected());

        try {
            CompositeType compositeType = new CompositeType("AppliationConfigurationObject",
                    "Application Configuration Composite Data",
                    AppConfigRowAttrNames,
                    AppConfigAttrDesc,
                    AppConfigAttrTypes);
            return new CompositeDataSupport(compositeType, map);
        } catch (OpenDataException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void validateContent() {
        // fire property change event at first error
        if (!nonEmptyString(hostName.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_HOSTNAME}));
            return;
        }
        if (!nonEmptyString(port.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_PORT}));
            return;
        }else{
            String portStringValue = port.getText();
            try {
                int portIntValue = Integer.parseInt(portStringValue);
                if (portIntValue <= 0 || portIntValue > 65525) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {APPLICATION_CONFIG_PROPERTY_PORT, new Integer(1), new Integer(65525)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {APPLICATION_CONFIG_PROPERTY_PORT, portStringValue}));
                return;
            }            
        }
        if (!nonEmptyString(startBlockChar.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR}));
            return;
        }else{
            String sbStringValue = startBlockChar.getText();
            try {
                int sbIntValue = Integer.parseInt(sbStringValue);
                if (sbIntValue < 1 || sbIntValue > 127) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR, new Integer(1), new Integer(127)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {APPLICATION_CONFIG_PROPERTY_STARTBLOCKCHAR, sbStringValue}));
                return;
            }
        }
        if (!nonEmptyString(endBlockChar.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR}));
            return;
        }else{
            String endbStringValue = endBlockChar.getText();
            try {
                int endbIntValue = Integer.parseInt(endbStringValue);
                if (endbIntValue < 1 || endbIntValue > 127) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR, new Integer(1), new Integer(127)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {APPLICATION_CONFIG_PROPERTY_ENDBLOCKCHAR, endbStringValue}));
                return;
            }
        }
        if (!nonEmptyString(endDataChar.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_ENDDATACHAR}));
            return;
        }else{
            String enddStringValue = endDataChar.getText();
            try {
                int enddIntValue = Integer.parseInt(enddStringValue);
                if (enddIntValue < 1 || enddIntValue > 127) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {APPLICATION_CONFIG_PROPERTY_ENDDATACHAR, new Integer(1), new Integer(127)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {APPLICATION_CONFIG_PROPERTY_ENDDATACHAR, enddStringValue}));
                return;
            }
        }
        if (!nonEmptyString(mllpv2Retries.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT}));
            return;
        }else{
            String mllpv2RetriesStrValue = mllpv2Retries.getText();
            try {
                int mllpv2RetriesValue = Integer.parseInt(mllpv2RetriesStrValue);
                if (mllpv2RetriesValue < 0 || mllpv2RetriesValue > 65525) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT, new Integer(0), new Integer(65525)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRIES_COUNT, mllpv2RetriesStrValue}));
                return;
            }
        }
        if (!nonEmptyString(mllpv2RetryInterval.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL}));
            return;
        }else{
            String mllpv2RetryStrValue = mllpv2RetryInterval.getText();
            try {
                int mllpv2RetryValue = Integer.parseInt(mllpv2RetryStrValue);
                if (mllpv2RetryValue < 0 || mllpv2RetryValue > 65525) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL, new Integer(0), new Integer(65525)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_RETRY_INTERVAL, mllpv2RetryStrValue}));
                return;
            }
        }
        if (!nonEmptyString(mllpv2TimeToWait.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK}));
            return;
        }else{
            String mllpv2TimeToWaitStrValue = mllpv2TimeToWait.getText();
            try {
                int mllpv2TimeToWaitValue = Integer.parseInt(mllpv2TimeToWaitStrValue);
                if (mllpv2TimeToWaitValue < 0 || mllpv2TimeToWaitValue > 65525) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK, new Integer(0), new Integer(65525)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {APPLICATION_CONFIG_PROPERTY_MLLPV2_TIME_TO_WAIT_FOR_ACKNAK, mllpv2TimeToWaitStrValue}));
                return;
            }
        }
        if (validateMSH.isSelected()) {
            if(processingID.getSelectedItem().toString().equals("")
                    || versionID.getSelectedItem().toString().equals("")){
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                     getLocalizedMessage(PARAMMSH_HAS_REQ_FIELDS, new Object[] {APPLICATION_CONFIG_PROPERTY_VALIDATEMSH}));
                return;
            }
        }
        if (sftEnabled.isSelected()) {
            String versionid = versionID.getSelectedItem().toString();
            if(!versionID.getSelectedItem().toString().equals("2.5") && !versionID.getSelectedItem().toString().equals("2.5.1")
                    && !versionID.getSelectedItem().toString().equals("2.6")){
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null,
                     getLocalizedMessage(PARAMSFT_HAS_REQ_FIELDS, new Object[] {APPLICATION_CONFIG_PROPERTY_SFT_ENABLED, new String(versionid)}));
                return;
            }
        }
        firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, null);
    }

    public void insertUpdate(DocumentEvent e) {
        validateContent();
    }

    public void removeUpdate(DocumentEvent e) {
        validateContent();
    }

    public void changedUpdate(DocumentEvent e) {
        validateContent();
    }

    public void actionPerformed(ActionEvent e) {
        Object select = e.getSource();
        if ( select == validateMSH ) {
            validateContent();
        }
        if ( select == sftEnabled ) {
            validateContent();
        }
    }

    private boolean nonEmptyString(String strToTest) {
        boolean nonEmpty = false;
        if (strToTest != null && strToTest.length() > 0) {
            nonEmpty = true;
        }
        return nonEmpty;
    }
}
