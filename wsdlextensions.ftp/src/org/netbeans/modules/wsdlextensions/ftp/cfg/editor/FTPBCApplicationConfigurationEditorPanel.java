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
package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.ftp.validator.Util;
import org.netbeans.modules.xml.wsdl.bindingsupport.appconfig.spi.CompositeDataEditorPanel;

/**
 *
 * @author jfu
 */
public class FTPBCApplicationConfigurationEditorPanel
        extends CompositeDataEditorPanel implements DocumentListener, ActionListener {
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ftp.cfg.editor.Bundle");

    private static final String PARAM_NOT_SPECIFIED = "PARAM_NOT_SPECIFIED";
    private static final String NUM_PARM_NOT_IN_RANGE = "NUM_PARAM_NOT_IN_RANGE";
    private static final String PARAM_HAS_TOBE_INT = "PARAM_HAS_TOBE_INT";
    private static final String KEYSTORE_NOT_SPECIFIED = "KEYSTORE_NOT_SPECIFIED";
    private static final String MISSING_PASSWORD = "MISSING_PASSWORD";
    private static final String REQUIRE_UD_STYLE_AND_CFG_FILE = "REQUIRE_UD_STYLE_AND_CFG_FILE";
    
    private JTextField ftpHost;
    private JTextField ftpPort;
    private JTextField ftpUser;
    private JPasswordField ftpPassword;
    private JComboBox ftpDirListStyle;
    private JCheckBox ftpUseUserDefinedDirListStyle;
    private JTextField ftpUserDefinedDirListStyle;
    private JTextField ftpUserDefinedDirListHeuristicCfgFile;
    private JComboBox ftpSecType;
    private JCheckBox enableCCC;
    private JTextField kstoreLoc;
    private JPasswordField kstorePassword;
    private JTextField keyAlias;
    private JPasswordField keyPassword;
    private JTextField tstoreLoc;
    private JPasswordField tstorePassword;
    private JTextField persistBaseLoc;
    
    private String appConfigName;
    
    private static final String APP_CFG_NAME = "configurationName";
    private static final String FTP_HOST_NAME = "Host";
    private static final String FTP_HOST_PORT = "Port";
    private static final String FTP_USER_ID = "User";
    private static final String FTP_PASSWORD = "Password";
    private static final String FTP_DIR_LIST_STYLE = "DirListStyle";
    private static final String FTP_USE_USER_DEFINED_DIR_LIST_STYLE = "UseUserDefinedDirListStyle";
    private static final String FTP_USER_DEFINED_DIR_LIST_STYLE = "UserDefinedDirListStyle";
    private static final String FTP_USER_DEFINED_DIR_LIST_STYLE_CFG = "UserDefinedDirListStyleConfig";
    private static final String FTP_SEC_TYPE = "SecuredType";
    private static final String FTP_ENABLE_CCC = "EnableCCC";
    private static final String FTP_KEY_STORE = "KeyStore";
    private static final String FTP_KEY_STORE_PASSWORD = "KeyStorePassword";
    private static final String FTP_KEY_ALIAS = "KeyAlias";
    private static final String FTP_KEY_PASSWORD = "KeyPassword";
    private static final String FTP_TRUST_STORE = "TrustStore";
    private static final String FTP_TRUST_STORE_PASSWORD = "TrustStorePassword";
    private static final String FTP_PERSISTENCE_BASE_DIR = "PersistenceBaseLoc";

    public FTPBCApplicationConfigurationEditorPanel(String appConfigName,
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
        setLayout(new GridLayout(17, 2));

        ftpHost = new JTextField();
        ftpHost.setText(compositeData == null ? "localhost" : (String) compositeData.get(FTP_HOST_NAME));

        ftpPort = new JTextField();
        ftpPort.setText(compositeData == null ? "21" : compositeData.get(FTP_HOST_PORT).toString());

        ftpUser = new JTextField();
        ftpUser.setText(compositeData == null ? "" : (String)compositeData.get(FTP_USER_ID));

        ftpPassword = new JPasswordField();
        ftpPassword.setText(compositeData == null ? "" : (String)compositeData.get(FTP_PASSWORD));

        ftpDirListStyle = new JComboBox(
                new Object[] {
                    "UNIX",
                    "AS400",
                    "AS400-UNIX",
                    "HCLFTPD 6.0.1.3",
                    "HCLFTPD 5.1",
                    "HP NonStop/Tandem",
                    "MPE",
                    "MSFTPD 2.0",
                    "MSP PDS (Fujitsu)",
                    "MSP PS (Fujitsu)",
                    "MVS GDG",
                    "MVS PDS",
                    "MVS Sequential",
                    "Netware 4.11",
                    "NT 3.5",
                    "NT 4.0",
                    "UNIX (EUC-JP)",
                    "UNIX (SJIS)",
                    "VM/ESA",
                    "VMS",
                    "VOS3 PDS (Hitachi)",
                    "VOS3 PS (Hitachi)",
                    "VOSK (Hitachi)"
        });
        
        String style = compositeData == null ? "UNIX" : (String)compositeData.get(FTP_DIR_LIST_STYLE);

        if ( style == null || style.trim().length() == 0 )
            style = "UNIX";
        ftpDirListStyle.setSelectedItem(style);

        ftpUseUserDefinedDirListStyle = new JCheckBox();
        Boolean useUD = (compositeData == null ? null : (Boolean)compositeData.get(FTP_USE_USER_DEFINED_DIR_LIST_STYLE));
        if ( useUD == null )
            ftpUseUserDefinedDirListStyle.setSelected(false);
        else
            ftpUseUserDefinedDirListStyle.setSelected(useUD.booleanValue());

        String udStyle = compositeData == null ? "" : (String)compositeData.get(FTP_USER_DEFINED_DIR_LIST_STYLE);

        ftpUserDefinedDirListStyle = new JTextField();
        ftpUserDefinedDirListStyle.setText(udStyle);
        
        String udConfigFile = compositeData == null ? "" : (String)compositeData.get(FTP_USER_DEFINED_DIR_LIST_STYLE_CFG);

        ftpUserDefinedDirListHeuristicCfgFile = new JTextField();
        ftpUserDefinedDirListHeuristicCfgFile.setText(udConfigFile);
        
        ftpSecType = new JComboBox(new Object[] {"None", "ExplicitSSL", "ImplicitSSL"});
        String selection = compositeData == null ? "None" : (String)compositeData.get(FTP_SEC_TYPE);
        if ( selection == null || selection.trim().length() == 0 )
            selection = "None";
        ftpSecType.setSelectedItem(selection);

        enableCCC = new JCheckBox();
        Boolean cccEnabled = (compositeData == null ? null : (Boolean)compositeData.get(FTP_ENABLE_CCC));
        if ( cccEnabled == null )
            enableCCC.setSelected(false);
        else
            enableCCC.setSelected(cccEnabled.booleanValue());
        
        kstoreLoc = new JTextField();
        kstoreLoc.setText(compositeData == null ? "" : (String)compositeData.get(FTP_KEY_STORE));

        kstorePassword = new JPasswordField();
        kstorePassword.setText(compositeData == null ? "" : (String)compositeData.get(FTP_KEY_STORE_PASSWORD));

        keyAlias = new JTextField();
        keyAlias.setText(compositeData == null ? "" : (String)compositeData.get(FTP_KEY_ALIAS));

        keyPassword = new JPasswordField();
        keyPassword.setText(compositeData == null ? "" : (String)compositeData.get(FTP_KEY_PASSWORD));

        tstoreLoc = new JTextField();
        tstoreLoc.setText(compositeData == null ? "" : (String)compositeData.get(FTP_TRUST_STORE));

        tstorePassword = new JPasswordField();
        tstorePassword.setText(compositeData == null ? "" : (String)compositeData.get(FTP_TRUST_STORE_PASSWORD));

        persistBaseLoc = new JTextField();
        persistBaseLoc.setText(compositeData == null ? "" : (String)compositeData.get(FTP_PERSISTENCE_BASE_DIR));

        add(new JLabel(getDisplayName(FTP_HOST_NAME)));
        add(ftpHost);
        add(new JLabel(getDisplayName(FTP_HOST_PORT)));
        add(ftpPort);
        add(new JLabel(getDisplayName(FTP_USER_ID)));
        add(ftpUser);
        add(new JLabel(getDisplayName(FTP_PASSWORD)));
        add(ftpPassword);

        add(new JLabel(getDisplayName(FTP_DIR_LIST_STYLE)));
        add(ftpDirListStyle);
        add(new JLabel(getDisplayName(FTP_USE_USER_DEFINED_DIR_LIST_STYLE)));
        add(ftpUseUserDefinedDirListStyle);
        add(new JLabel(getDisplayName(FTP_USER_DEFINED_DIR_LIST_STYLE)));
        add(ftpUserDefinedDirListStyle);
        add(new JLabel(getDisplayName(FTP_USER_DEFINED_DIR_LIST_STYLE_CFG)));
        add(ftpUserDefinedDirListHeuristicCfgFile);
        
        add(new JLabel(getDisplayName(FTP_SEC_TYPE)));
        add(ftpSecType);
        add(new JLabel(getDisplayName(FTP_ENABLE_CCC)));
        add(enableCCC);
        add(new JLabel(getDisplayName(FTP_KEY_STORE)));
        add(kstoreLoc);
        add(new JLabel(getDisplayName(FTP_KEY_STORE_PASSWORD)));
        add(kstorePassword);
        add(new JLabel(getDisplayName(FTP_KEY_ALIAS)));
        add(keyAlias);
        add(new JLabel(getDisplayName(FTP_KEY_PASSWORD)));
        add(keyPassword);
        add(new JLabel(getDisplayName(FTP_TRUST_STORE)));
        add(tstoreLoc);
        add(new JLabel(getDisplayName(FTP_TRUST_STORE_PASSWORD)));
        add(tstorePassword);
        add(new JLabel(getDisplayName(FTP_PERSISTENCE_BASE_DIR)));
        add(persistBaseLoc);

        ftpHost.getDocument().addDocumentListener(this);
        ftpPort.getDocument().addDocumentListener(this);
        ftpUser.getDocument().addDocumentListener(this);
        ftpPassword.getDocument().addDocumentListener(this);
        
        ftpUserDefinedDirListHeuristicCfgFile.getDocument().addDocumentListener(this);
        ftpUserDefinedDirListStyle.getDocument().addDocumentListener(this);
        
        kstoreLoc.getDocument().addDocumentListener(this);
        kstorePassword.getDocument().addDocumentListener(this);
        keyAlias.getDocument().addDocumentListener(this);
        keyPassword.getDocument().addDocumentListener(this);
        tstoreLoc.getDocument().addDocumentListener(this);
        tstorePassword.getDocument().addDocumentListener(this);
        ftpSecType.addActionListener(this);
        ftpDirListStyle.addActionListener(this);
        ftpUseUserDefinedDirListStyle.addActionListener(this);
        persistBaseLoc.getDocument().addDocumentListener(this);
        
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
        map.put(FTP_HOST_NAME, ftpHost.getText());
        map.put(FTP_HOST_PORT, Integer.parseInt(ftpPort.getText()));
        map.put(FTP_USER_ID, ftpUser.getText());
        map.put(FTP_PASSWORD, ftpPassword.getPassword() != null ? new String(ftpPassword.getPassword()) : "");

        map.put(FTP_DIR_LIST_STYLE, ftpDirListStyle.getSelectedItem() != null ?  ftpDirListStyle.getSelectedItem().toString() : "");
        map.put(FTP_USE_USER_DEFINED_DIR_LIST_STYLE, new Boolean(ftpUseUserDefinedDirListStyle.isSelected()));
        map.put(FTP_USER_DEFINED_DIR_LIST_STYLE, ftpUserDefinedDirListStyle.getText());
        map.put(FTP_USER_DEFINED_DIR_LIST_STYLE_CFG, ftpUserDefinedDirListHeuristicCfgFile.getText());

        map.put(FTP_SEC_TYPE, ftpSecType.getSelectedItem() != null ? ftpSecType.getSelectedItem().toString() : "None");
        map.put(FTP_ENABLE_CCC, enableCCC.isSelected());
        map.put(FTP_KEY_STORE, kstoreLoc.getText());
        map.put(FTP_KEY_STORE_PASSWORD, kstorePassword.getPassword() != null ? new String(kstorePassword.getPassword()) : "");
        map.put(FTP_KEY_ALIAS, keyAlias.getText());
        map.put(FTP_KEY_PASSWORD, keyPassword.getPassword() != null ? new String(keyPassword.getPassword()) : "");
        map.put(FTP_TRUST_STORE, tstoreLoc.getText());
        map.put(FTP_TRUST_STORE_PASSWORD, tstorePassword.getPassword() != null ? new String(tstorePassword.getPassword()) : "");
        map.put(FTP_PERSISTENCE_BASE_DIR, persistBaseLoc.getText() != null ? persistBaseLoc.getText().trim() : "");

        try {
            CompositeType compositeType = new CompositeType("FTPBCAppConfig",
                    "FTPBC Application Configuration CompositeType",
                    new String[]{
                        APP_CFG_NAME, 
                        FTP_HOST_NAME,
                        FTP_HOST_PORT,
                        FTP_USER_ID,
                        FTP_PASSWORD,
                        FTP_DIR_LIST_STYLE,
                        FTP_USE_USER_DEFINED_DIR_LIST_STYLE,
                        FTP_USER_DEFINED_DIR_LIST_STYLE,
                        FTP_USER_DEFINED_DIR_LIST_STYLE_CFG,
                        FTP_SEC_TYPE,
                        FTP_ENABLE_CCC,
                        FTP_KEY_STORE,
                        FTP_KEY_STORE_PASSWORD,
                        FTP_KEY_ALIAS,
                        FTP_KEY_PASSWORD,
                        FTP_TRUST_STORE,
                        FTP_TRUST_STORE_PASSWORD,
                        FTP_PERSISTENCE_BASE_DIR
                    },
                    new String[]{
                        getDisplayName(APP_CFG_NAME),
                        getDisplayName(FTP_HOST_NAME),
                        getDisplayName(FTP_HOST_PORT),
                        getDisplayName(FTP_USER_ID),
                        getDisplayName(FTP_PASSWORD),
                        getDisplayName(FTP_DIR_LIST_STYLE),
                        getDisplayName(FTP_USE_USER_DEFINED_DIR_LIST_STYLE),
                        getDisplayName(FTP_USER_DEFINED_DIR_LIST_STYLE),
                        getDisplayName(FTP_USER_DEFINED_DIR_LIST_STYLE_CFG),
                        getDisplayName(FTP_SEC_TYPE),
                        getDisplayName(FTP_ENABLE_CCC),
                        getDisplayName(FTP_KEY_STORE),
                        getDisplayName(FTP_KEY_STORE_PASSWORD),
                        getDisplayName(FTP_KEY_ALIAS),
                        getDisplayName(FTP_KEY_PASSWORD),
                        getDisplayName(FTP_TRUST_STORE),
                        getDisplayName(FTP_TRUST_STORE_PASSWORD),
                        getDisplayName(FTP_PERSISTENCE_BASE_DIR)
                    },
                    new OpenType[]{
                        SimpleType.STRING, 
                        SimpleType.STRING, 
                        SimpleType.INTEGER,
                        SimpleType.STRING, 
                        SimpleType.STRING, 
                        SimpleType.STRING,
                        SimpleType.BOOLEAN, 
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
                        SimpleType.STRING
                });
            return new CompositeDataSupport(compositeType, map);
        } catch (OpenDataException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void validateContent() {
        // fire property change event at first error
        if (Util.isEmpty(ftpHost.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {FTP_HOST_NAME}));
            return;
        }

        if (Util.isEmpty(ftpPort.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {FTP_HOST_PORT}));
            return;
        }
        else {
            String portStringValue = ftpPort.getText();
            try {
                int portIntValue = Integer.parseInt(portStringValue);
                if (portIntValue <= 0 || portIntValue > 65525) {
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                            getLocalizedMessage(NUM_PARM_NOT_IN_RANGE, new Object[] {FTP_HOST_PORT, new Integer(1), new Integer(65525)}));
                    return;
                }
            } catch (Exception e) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                        getLocalizedMessage(PARAM_HAS_TOBE_INT, new Object[] {FTP_HOST_PORT, portStringValue}));
                return;
            }
        }
        
        if (Util.isEmpty(ftpUser.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {FTP_USER_ID}));
            return;
        }

        // validate UD style
        // when ud enabled, user def style and cfg file both need to be specified
        if ( ftpUseUserDefinedDirListStyle.isSelected() ) {
            if ( Util.isEmpty(ftpUserDefinedDirListStyle.getText()) || Util.isEmpty(ftpUserDefinedDirListHeuristicCfgFile.getText()) ) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                        getLocalizedMessage(REQUIRE_UD_STYLE_AND_CFG_FILE, new Object[] {ftpUserDefinedDirListStyle.getText(), ftpUserDefinedDirListHeuristicCfgFile.getText()}));
                return;
            }
        }
        
        // password can be blank for some ftp server
        String secureType = ftpSecType.getSelectedItem() != null ? ftpSecType.getSelectedItem().toString() : "None";

        if ( !secureType.equals("None") ) {
            // when secure type is not "None"
            // need at least key store or trust store
            if ( Util.isEmpty(kstoreLoc.getText()) && Util.isEmpty(tstoreLoc.getText()) ) {
                firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                        getLocalizedMessage(KEYSTORE_NOT_SPECIFIED, null));
                return;
            }
            else {
                if ( !Util.isEmpty(kstoreLoc.getText()) 
                        && Util.isEmpty( kstorePassword.getPassword() != null ? new String(kstorePassword.getPassword()) : "")) {
                    // key store specified but password is blank - a scenario viewed as invalid
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                            getLocalizedMessage(MISSING_PASSWORD, new Object[] {FTP_KEY_STORE, kstoreLoc.getText(), FTP_KEY_STORE_PASSWORD}));
                    return;
                }
                if ( !Util.isEmpty(tstoreLoc.getText()) 
                        && Util.isEmpty( tstorePassword.getPassword() != null ? new String(tstorePassword.getPassword()) : "")) {
                    // key store specified but password is blank - a scenario viewed as invalid
                    firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                            getLocalizedMessage(MISSING_PASSWORD, new Object[] {FTP_TRUST_STORE, tstoreLoc.getText(), FTP_TRUST_STORE_PASSWORD}));
                    return;
                }
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
        if ( select instanceof JComboBox && (select == ftpSecType || select == ftpDirListStyle ) ) {
            validateContent();
        }
        if ( select == ftpUseUserDefinedDirListStyle ) {
            validateContent();
        }
    }
}
