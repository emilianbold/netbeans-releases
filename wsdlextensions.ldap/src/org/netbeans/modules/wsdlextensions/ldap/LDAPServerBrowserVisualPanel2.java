/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.wsdlextensions.ldap;

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.ldap.utils.LdapConnection;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class LDAPServerBrowserVisualPanel2 extends JPanel {

    private LdapConnection conn = null;
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ldap.Bundle");
    private String domainFormate = "((\\w+)|(\\w+-\\w+))";
	private String pt = "[a-zA-Z_0-9]+";
    private WSDLComponent mWSDLComponent = null;
    private boolean mLDAPServerMode = true;

    /** Creates new form LDAPServerBrowserVisualPanel2 */
    public LDAPServerBrowserVisualPanel2() {
        String url = org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
        "LDAPServerBrowserVisualPanel2.IMG_CASA");
        Image img = org.openide.util.Utilities.loadImage(url);
		mCASAImg = new ImageIcon(img);   
        initComponents();
        conn = new LdapConnection();
        init();
        initListeners();
    }

    private void init() {
    	jDescriptionDisplayPane.setEditable(false);
    	jDescriptionDisplayPane.setOpaque(false);
        buttonGroup1.add(jRadioButtonServer);
        buttonGroup1.add(jRadioButtonFile);
        jRadioButtonServer.setSelected(true);
        jRadioButtonFile.setSelected(false);
        jTextFieldDataFilePath.setEnabled(false);
        jTextFieldSchemaFilePath.setEnabled(false);
        jButtonBrowserDataFile.setEnabled(false);
        jButtonBrowserSchemaFile.setEnabled(false);
        jTextFieldRootDN.setText("dc=sun,dc=com");
    }

    @Override
    public String getName() {
        return "LDAP Server Setting";
    }

    public void read(WizardDescriptor wd) {
        // if from WSDL Wizard, do not show toggles anymore
        if (wd.getProperty("TEMP_WSDLMODEL") != null) {          
            if (mLDAPServerMode) {
                // selected LDAP Server mode; do not show toggles here
                jRadioButtonServer.doClick();
                enableLDIFSection(false);
                enableLDAPServerSection(true);
            } else {
                // selected LDIF File mode; do not show toggles here
                jRadioButtonFile.doClick();
                enableLDIFSection(true);
                enableLDAPServerSection(false);
            }
                        
            jRadioButtonServer.setVisible(false);
            jRadioButtonFile.setVisible(false);    
            
        } else {
            // keep old functionality
            jRadioButtonServer.setVisible(true);
            jRadioButtonFile.setVisible(true);  
            enableLDIFSection(true);
            enableLDAPServerSection(true);
        }
        
        FileObject destDir = Templates.getTargetFolder(wd);    
        if ((destDir != null) && (FileUtil.toFile(destDir) != null)) {
            File fileDestDir = FileUtil.toFile(destDir);
            wd.putProperty("TARGETFOLDER_PATH", fileDestDir.getAbsolutePath());
        }  
    }
    public void store(WizardDescriptor wd) {

        if (jRadioButtonServer.isSelected()) {
            if (!jTextFieldLDAPServerURL.getText().trim().equals(conn.getLocation()) ||
                    !jTextFieldRootDN.getText().trim().equals(conn.getDn())) {
                conn.setConnectionReconnect(true);
            }
            conn.setUrl(jTextFieldLDAPServerURL.getText());
            conn.setDn(jTextFieldRootDN.getText());

            wd.putProperty("LDAP_DATA_FROM_TYPE", "FROM_SERVER");
        }
        if (jRadioButtonFile.isSelected()) {
            if (!jTextFieldDataFilePath.getText().equals(wd.getProperty("LDAP_DATA_FILE_PATH"))
                || !jTextFieldSchemaFilePath.getText().equals(wd.getProperty("LDAP_SCHEMA_FILE_PATH"))) {
                conn.setConnectionReconnect(true);
            }
            wd.putProperty("LDAP_DATA_FILE_PATH", jTextFieldDataFilePath.getText());
            wd.putProperty("LDAP_SCHEMA_FILE_PATH", jTextFieldSchemaFilePath.getText());
            wd.putProperty("LDAP_ROOT_DN", jTextFieldRootDN.getText());
            wd.putProperty("LDAP_DATA_FROM_TYPE", "FROM_FILES");
        }

        wd.putProperty("LDAP_CONNECTION", conn);
//        JOptionPane.showMessageDialog(null, conn.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
        if (mLDAPServerMode) {
            wd.putProperty("FROM_LDAP_SERVER", new Boolean(true));
            wd.putProperty("FROM_LDIF_FILE", new Boolean(false));
        } else {
            wd.putProperty("FROM_LDAP_SERVER", new Boolean(false));
            wd.putProperty("FROM_LDIF_FILE", new Boolean(true));
        }          
    }

/*    public String validateInput() {
        if(!conn.isConnectionReconnect()){
            return "";
        }
        String dn = jTextFieldRootDN.getText().trim();
        if ("".equals(dn)) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.Root_dn_null");
        }
        if (!isLDAPDN(dn)) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.invalid_root_dn");
        }
        if (jRadioButtonServer.isSelected()) {
            return validateServerInput();
        }
        if (jRadioButtonFile.isSelected()) {
            return validateDataFileInput();
        }
        return "";
    }*/

    /**
     * The wsdl component for this wizard panel
     * 
     * @param wsdlComponent
     */
    public void setWSDLComponent(WSDLComponent wsdlComponent) {
        mWSDLComponent = wsdlComponent;
    }
    
    /**
     * Sets the ldap server mode flag
     * @param mode
     */
    public void setLDAPServerMode(boolean mode) {
        mLDAPServerMode = mode;
    }
    
/*    private String validateDataFileInput() {
        String data = jTextFieldDataFilePath.getText().trim();
        String schema = jTextFieldSchemaFilePath.getText().trim();
        if ("".equals(data) || "".equals(schema)) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.input_both_data_schema_ldif_path");
        }
        File f = new File(data);
        if (!f.exists()) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.Data_ldif_file_not_exist");
        }
        if (!f.isFile()) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.select_data_ldif_file_null");
        }
        if (!data.endsWith(".ldif")) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.must_be_ldif_format");
        }
        f = new File(schema);
        if (!f.exists()) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.schema_ldif_file_not_exist");
        }
        if (!f.isFile()) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.select_schema_ldif_file_null");
        }
        if (!schema.endsWith(".ldif")) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.must_be_ldif_format");
        }
        return "";
    }

    private String validateServerInput() {
        String url = jTextFieldLDAPServerURL.getText().trim();

        if ("".equals(url)) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.ldap_server_url_null");
        }
        if (!isLDAPURL(url)) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.invalid_ldap_server_url");
        }
        if (!conn.validateParam()) {
            return mMessages.getString("LDAPServerBrowserVisualPanel2.can_not_connect_to_server");
        }
        return "";
    }*/

    private boolean isLDAPDN(String dn) {
        String trimmed = dn.replaceAll(" ", "");
        String usedomain = "(o=" + domainFormate + "(\\." + domainFormate + ")*)";
        //String usedc = "(dc=" + domainFormate + "(\\,dc=" + domainFormate + ")*)";
		String usedc = "(" + pt + "=" + domainFormate + "(\\," + pt + "=" + domainFormate + ")*)";
        String dnRegex = "(" + usedomain + "|" + usedc + ")";
        return Pattern.matches(dnRegex, trimmed);
    }

    private boolean isLDAPURL(String url) {
        String protocol = "((ldap://)|(ldaps://))";
        String port = "((:[0-9]+)*)";
        String ipAddress0 = "(2[0-4]\\d)|(25[0-5])";
        String ipAddress1 = "1\\d{2}";
        String ipAddress2 = "[1-9]\\d";
        String ipAddress3 = "\\d";
        String ipAddress = "(" + ipAddress0 + ")|(" + ipAddress1 + ")|(" + ipAddress2 + ")|(" + ipAddress3 + ")";
        ipAddress = "(" + ipAddress + ")\\.(" + ipAddress + ")\\.(" + ipAddress + ")\\.(" + ipAddress + ")";
        String domain = "(" + domainFormate + "(\\." + domainFormate + ")*)";
        String regexIP = "(" + protocol + ipAddress + port + ")";
        String regexLocalhost = "(" + protocol + domain + port + ")";
        String regex = "(" + regexIP + "|" + regexLocalhost + ")";
        return Pattern.matches(regex, url);
    }
    
    private void enableLDIFSection(boolean enable) {
        jRadioButtonServer.setVisible(enable);
        jRadioButtonFile.setVisible(enable);
        jLabelRootDN1.setVisible(enable);
        jLabelLDAPServerURL1.setVisible(enable);
        jTextFieldDataFilePath.setVisible(enable);
        jTextFieldSchemaFilePath.setVisible(enable);
        jButtonBrowserDataFile.setVisible(enable);
        jButtonBrowserSchemaFile.setVisible(enable);
        jTextFieldDataFilePath.setText("C:\\Temp\\datafile.ldif");
        jTextFieldSchemaFilePath.setText("C:\\Temp\\schemafile.ldif");
    }
    
    private void enableLDAPServerSection(boolean enable) {        
        jLabelLDAPServerURL.setVisible(enable);
        jTextFieldLDAPServerURL.setVisible(enable);
        jButtonAdvanced.setVisible(enable);
        jTextFieldLDAPServerURL.setText("ldap://localhost:389");
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabelRootDN = new javax.swing.JLabel();
        jTextFieldRootDN = new javax.swing.JTextField();
        jRadioButtonServer = new javax.swing.JRadioButton();
        jLabelLDAPServerURL = new javax.swing.JLabel();
        jTextFieldLDAPServerURL = new javax.swing.JTextField();
        jButtonAdvanced = new javax.swing.JButton();
        jRadioButtonFile = new javax.swing.JRadioButton();
        jLabelLDAPServerURL1 = new javax.swing.JLabel();
        jButtonBrowserSchemaFile = new javax.swing.JButton();
        jTextFieldDataFilePath = new javax.swing.JTextField();
        jLabelRootDN1 = new javax.swing.JLabel();
        jTextFieldSchemaFilePath = new javax.swing.JTextField();
        jButtonBrowserDataFile = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jDescriptionScrollPane = new javax.swing.JScrollPane();
        jDescriptionDisplayPane = new javax.swing.JTextPane();
        mDoc = jDescriptionDisplayPane.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};
        Style def = StyleContext.getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = mDoc.addStyle("regular", def);
        Style s = mDoc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabelRootDN.setLabelFor(jTextFieldRootDN);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRootDN, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jLabelRootDN.text")); // NOI18N
        jLabelRootDN.setName("jLabelRootDN"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabelRootDN, gridBagConstraints);

        jTextFieldRootDN.setToolTipText(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldRootDN.desc")); // NOI18N
        jTextFieldRootDN.setName("jTextFieldRootDN"); // NOI18N
        jTextFieldRootDN.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldRootDNFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldRootDNFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jTextFieldRootDN, gridBagConstraints);
        jTextFieldRootDN.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldRootDN.RootDN")); // NOI18N
        jTextFieldRootDN.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldRootDN.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonServer, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jRadioButtonServer.text")); // NOI18N
        jRadioButtonServer.setName("jRadioButtonServer"); // NOI18N
        jRadioButtonServer.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonServerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jRadioButtonServer, gridBagConstraints);
        jRadioButtonServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jRadioButtonServer.desc")); // NOI18N

        jLabelLDAPServerURL.setLabelFor(jTextFieldLDAPServerURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelLDAPServerURL, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jLabelLDAPServerURLS.text")); // NOI18N
        jLabelLDAPServerURL.setName("jLabelLDAPServerURL"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jLabelLDAPServerURL, gridBagConstraints);

        jTextFieldLDAPServerURL.setToolTipText(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldLDAPServerURL.desc")); // NOI18N
        jTextFieldLDAPServerURL.setName("jTextFieldLDAPServerURL"); // NOI18N
        jTextFieldLDAPServerURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldLDAPServerURLFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldLDAPServerURLFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        jPanel1.add(jTextFieldLDAPServerURL, gridBagConstraints);
        jTextFieldLDAPServerURL.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldLDAPServerURL.LDAPServerURL")); // NOI18N
        jTextFieldLDAPServerURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldLDAPServerURL.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAdvanced, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jButtonAdvanced.text")); // NOI18N
        jButtonAdvanced.setName("jButtonAdvanced"); // NOI18N
        jButtonAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdvancedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jButtonAdvanced, gridBagConstraints);
        jButtonAdvanced.getAccessibleContext().setAccessibleDescription("");

        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFile, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jRadioButtonFile.text")); // NOI18N
        jRadioButtonFile.setName("jRadioButtonFile"); // NOI18N
        jRadioButtonFile.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonFileStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jRadioButtonFile, gridBagConstraints);
        jRadioButtonFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jRadioButtonFile.desc")); // NOI18N

        jLabelLDAPServerURL1.setLabelFor(jTextFieldDataFilePath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelLDAPServerURL1, "Da&ta LDIF Path:");
        jLabelLDAPServerURL1.setName("jLabelLDAPServerURL1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jLabelLDAPServerURL1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowserSchemaFile, "Brows&e");
        jButtonBrowserSchemaFile.setName("jButtonBrowserSchemaFile"); // NOI18N
        jButtonBrowserSchemaFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowserSchemaFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel1.add(jButtonBrowserSchemaFile, gridBagConstraints);
        jButtonBrowserSchemaFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jButtonBrowserSchemaFile.desc")); // NOI18N

        jTextFieldDataFilePath.setToolTipText(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldDataFilePath.desc")); // NOI18N
        jTextFieldDataFilePath.setName("jTextFieldDataFilePath"); // NOI18N
        jTextFieldDataFilePath.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldDataFilePathFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldDataFilePathFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        jPanel1.add(jTextFieldDataFilePath, gridBagConstraints);
        jTextFieldDataFilePath.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldDataFilePath.text")); // NOI18N
        jTextFieldDataFilePath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldDataFilePath.desc")); // NOI18N

        jLabelRootDN1.setLabelFor(jTextFieldSchemaFilePath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRootDN1, "Sche&ma LDIF Path:");
        jLabelRootDN1.setName("jLabelRootDN1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jLabelRootDN1, gridBagConstraints);

        jTextFieldSchemaFilePath.setToolTipText(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldSchemaFilePath.desc")); // NOI18N
        jTextFieldSchemaFilePath.setName("jTextFieldSchemaFilePath"); // NOI18N
        jTextFieldSchemaFilePath.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldSchemaFilePathFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldSchemaFilePathFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        jPanel1.add(jTextFieldSchemaFilePath, gridBagConstraints);
        jTextFieldSchemaFilePath.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldSchemaFilePath.text")); // NOI18N
        jTextFieldSchemaFilePath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jTextFieldSchemaFilePath.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowserDataFile, "&Browse");
        jButtonBrowserDataFile.setName("jButtonBrowserDataFile"); // NOI18N
        jButtonBrowserDataFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowserDataFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel1.add(jButtonBrowserDataFile, gridBagConstraints);
        jButtonBrowserDataFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class, "LDAPServerBrowserVisualPanel2.jButtonBrowserDataFile.desc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        add(jPanel1, gridBagConstraints);

        jPanel2.setName("jPanel2"); // NOI18N

        jDescriptionScrollPane.setToolTipText("Field Description Display Pane");
        jDescriptionScrollPane.setName("jDescriptionlPane"); // NOI18N

        jDescriptionDisplayPane.setBackground(new java.awt.Color(240, 240, 240));
        jDescriptionDisplayPane.setToolTipText("Field Description Display Pane");
        jDescriptionDisplayPane.setName("jDescriptionDisplayPane"); // NOI18N
        jDescriptionScrollPane.setViewportView(jDescriptionDisplayPane);
        jDescriptionDisplayPane.getAccessibleContext().setAccessibleName("jDescriptionDisplayPane");
        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jDescriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap(166, Short.MAX_VALUE)
                .add(jDescriptionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jDescriptionScrollPane.getAccessibleContext().setAccessibleName("jDescriptionlPane");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.5;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdvancedActionPerformed
        // TODO add your handling code here:
        AdvancedLoginDialog dlg = new AdvancedLoginDialog(new Frame(), true, conn);
        dlg.setVisible(true);
    }//GEN-LAST:event_jButtonAdvancedActionPerformed

private void jButtonBrowserDataFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowserDataFileActionPerformed
// TODO add your handling code here:
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Please select data ldif file");

    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    String path = jTextFieldDataFilePath.getText();
    if (path.length() > 0) {
        File f = new File(path);
        if (f.exists()) {
            if (f.isDirectory()) {
                chooser.setSelectedFile(f);
            } else {
                chooser.setSelectedFile(new File(f.getAbsolutePath()));
            }
        }
    }

    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        jTextFieldDataFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}//GEN-LAST:event_jButtonBrowserDataFileActionPerformed

private void jButtonBrowserSchemaFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowserSchemaFileActionPerformed
// TODO add your handling code here:
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Please select data ldif file");

    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    String path = jTextFieldSchemaFilePath.getText();
    if (path.length() > 0) {
        File f = new File(path);
        if (f.exists()) {
            if (f.isDirectory()) {
                chooser.setSelectedFile(f);
            } else {
                chooser.setSelectedFile(new File(f.getAbsolutePath()));
            }
        }
    }

    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        jTextFieldSchemaFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}//GEN-LAST:event_jButtonBrowserSchemaFileActionPerformed

private void jRadioButtonFileStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonFileStateChanged
// TODO add your handling code here:
    if (jRadioButtonFile.isSelected()) {
        jTextFieldDataFilePath.setEnabled(true);
        jTextFieldSchemaFilePath.setEnabled(true);
        jButtonBrowserDataFile.setEnabled(true);
        jButtonBrowserSchemaFile.setEnabled(true);
    } else {
        jTextFieldDataFilePath.setEnabled(false);
        jTextFieldSchemaFilePath.setEnabled(false);
        jButtonBrowserDataFile.setEnabled(false);
        jButtonBrowserSchemaFile.setEnabled(false);
    }
}//GEN-LAST:event_jRadioButtonFileStateChanged

private void jRadioButtonServerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonServerStateChanged
// TODO add your handling code here:
    if (jRadioButtonServer.isSelected()) {
        jTextFieldLDAPServerURL.setEnabled(true);
//        jTextFieldRootDN.setEnabled(true);
        jButtonAdvanced.setEnabled(true);
    } else {
        jTextFieldLDAPServerURL.setEnabled(false);
//        jTextFieldRootDN.setEnabled(false);
        jButtonAdvanced.setEnabled(false);
    }
}//GEN-LAST:event_jRadioButtonServerStateChanged

private void jTextFieldRootDNFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldRootDNFocusGained
// TODO add your handling code here:
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldRootDNFocusGained

private void jTextFieldRootDNFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldRootDNFocusLost
// TODO add your handling code here:
	clearDescription();
}//GEN-LAST:event_jTextFieldRootDNFocusLost

private void jTextFieldLDAPServerURLFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldLDAPServerURLFocusGained
// TODO add your handling code here:
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldLDAPServerURLFocusGained

private void jTextFieldLDAPServerURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldLDAPServerURLFocusLost
// TODO add your handling code here:
	clearDescription();
}//GEN-LAST:event_jTextFieldLDAPServerURLFocusLost

private void jTextFieldDataFilePathFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDataFilePathFocusGained
// TODO add your handling code here:
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldDataFilePathFocusGained

private void jTextFieldDataFilePathFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDataFilePathFocusLost
// TODO add your handling code here:
	clearDescription();
}//GEN-LAST:event_jTextFieldDataFilePathFocusLost

private void jTextFieldSchemaFilePathFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldSchemaFilePathFocusGained
// TODO add your handling code here:
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldSchemaFilePathFocusGained

private void jTextFieldSchemaFilePathFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldSchemaFilePathFocusLost
// TODO add your handling code here:
	clearDescription();
}//GEN-LAST:event_jTextFieldSchemaFilePathFocusLost

private void clearDescription() {
    jDescriptionDisplayPane.setText("");
}

private void updateDescription(java.awt.event.FocusEvent evt) {
	jDescriptionDisplayPane.setText("");
	// The image must first be wrapped in a style
	Style style =  mDoc.addStyle("StyleName", null);
	StyleConstants.setIcon(style, mCASAImg);
	String[] desc = null;
	boolean casaEdited = false;

    if (evt.getSource() == jTextFieldRootDN) {
        desc = new String[]{" Root DN ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "LDAPServerBrowserVisualPanel2.jTextFieldRootDN.desc")};
        casaEdited = true;
    } else if (evt.getSource() == jTextFieldLDAPServerURL){
        desc = new String[]{" LDAP Server Location ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "LDAPServerBrowserVisualPanel2.jTextFieldLDAPServerURL.desc")};
        casaEdited = true;
    } else if (evt.getSource() == jTextFieldDataFilePath){
        desc = new String[]{" Data LDIF Path ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "LDAPServerBrowserVisualPanel2.jTextFieldDataFilePath.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jTextFieldSchemaFilePath){
        desc = new String[]{" Schema LDIF Path ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "LDAPServerBrowserVisualPanel2.jTextFieldSchemaFilePath.desc")};
         casaEdited = true;
    } 
    if (desc != null) {
        try {
            mDoc.insertString(mDoc.getLength(), desc[0],
                    mDoc.getStyle(mStyles[0]));
            mDoc.insertString(mDoc.getLength(), desc[1],
                    mDoc.getStyle(mStyles[1]));

       // Insert the image
            if (casaEdited) {
            }
 
            jDescriptionDisplayPane.setCaretPosition(0);
        } catch(BadLocationException ble) {
            mLogger.log(Level.FINER, ble.getMessage());
        }
        return;
    }
}  
	private void initListeners() {
	    if (mActionListener == null)  {
	        mActionListener = new MyActionListener();
	    }
	    
	    if (mDocumentListener == null) {
	        mDocumentListener = new MyDocumentListener();
	    }
	    
	    jTextFieldDataFilePath.addActionListener(mActionListener);
	    jTextFieldSchemaFilePath.addActionListener(mActionListener);
	    jTextFieldRootDN.addActionListener(mActionListener);
		jTextFieldLDAPServerURL.addActionListener(mActionListener);

	    jTextFieldDataFilePath.getDocument().addDocumentListener(mDocumentListener); 	
	    jTextFieldSchemaFilePath.getDocument().addDocumentListener(mDocumentListener);
		jTextFieldRootDN.getDocument().addDocumentListener(mDocumentListener);
		jTextFieldLDAPServerURL.getDocument().addDocumentListener(mDocumentListener);
	}
	/**
	 * Route the property change event to this panel
	 */
	public void doFirePropertyChange(String name, Object oldValue, Object newValue) {           
	    firePropertyChange(name, oldValue, 
	            newValue);         
	}      
	
	public class MyActionListener implements ActionListener {
	    public void actionPerformed(ActionEvent evt) {
	        handleActionPerformed(evt);
	    }
	}
    public class MyDocumentListener implements DocumentListener {
    	// Handle insertions into the text field
            public void insertUpdate(DocumentEvent event) {
            	validateInput(true);
            }

    	// Handle deletions	from the text field
            public void removeUpdate(DocumentEvent event) {
            	validateInput(true);
            }

    	// Handle changes to the text field
            public void changedUpdate(DocumentEvent event) {
            	validateInput(true);
            }
                 
        }	
    public LDAPError validateInput() {
        if(conn.isConnectionReconnect()){
        	return validateInput(false);
        }
        return new LDAPError();
    }    
    
    public LDAPError validateInput(boolean fireEvent) {  
        // Validate LDAP Selected RDN
    	LDAPError ldapError = validateRootDN(fireEvent);
		if(jRadioButtonServer.isSelected()) 
		{
			if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(ldapError.getErrorMode())) {
            // Validate LDAP Server Location
				ldapError = validateLDAPServerLocation(fireEvent);
			}
		}
		if (jRadioButtonFile.isSelected()) {
			if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(ldapError.getErrorMode())) {
				// Validate Data File Path
        		ldapError = validateDataFilePath(fireEvent);  
				if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(ldapError.getErrorMode())) {
					// Validate Schema Data File Path
					ldapError = validateSchemaFilePath(fireEvent);
				}
			}
		}

        if (fireEvent) {
            ErrorPropagator.doFirePropertyChange(ldapError.getErrorMode(), null,
                    ldapError.getErrorMessage(), this);
        }      
        return ldapError;
    }  

    private void handleActionPerformed(ActionEvent evt) {

    }  

    /**
     * Trims input text and returns null, if blank.
     *
     * @param text
     * @return trimmed text, if blank returns null.
     */
    private String trimTextField(String text) {
        if (text == null) {
            return text;
        }
        String trimmedText = text.trim();
        if (trimmedText.length() == 0) {
            return null;
        }
        return text.trim();
    }  

    private LDAPError validateRootDN(boolean fireEvent) {
        LDAPError ldapError = new LDAPError();
        String rootDN = trimTextField(jTextFieldRootDN.getText());
        if (rootDN == null || rootDN.equals("")) {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                        "LDAPServerBrowserVisualPanel2.Root_dn_null"),
                        jDescriptionDisplayPane);
            }  
            ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
            						  "LDAPServerBrowserVisualPanel2.Root_dn_null"));
            ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
        } else {   
			if(!isLDAPDN(rootDN)){
				if (fireEvent) {
					ErrorPropagator.doFirePropertyChange(
							ExtensibilityElementConfigurationEditorComponent.
							PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
							"LDAPServerBrowserVisualPanel2.invalid_root_dn"),
							jDescriptionDisplayPane);
				}  
				ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
										  "LDAPServerBrowserVisualPanel2.invalid_root_dn"));
				ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
			}
		}
        if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "", jDescriptionDisplayPane);            
        }
        return ldapError;
    }  

    private LDAPError validateLDAPServerLocation(boolean fireEvent){
        LDAPError ldapError = new LDAPError();
        String serverLoc = trimTextField(jTextFieldLDAPServerURL.getText());
        if (serverLoc == null || serverLoc.equals("")) {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                        "LDAPServerBrowserVisualPanel2.ldap_server_url_null"),
                        jDescriptionDisplayPane);
            }  
            ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                          "LDAPServerBrowserVisualPanel2.ldap_server_url_null"));
            ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
        } else if(!isLDAPURL(serverLoc)){
                if (fireEvent) {
                        ErrorPropagator.doFirePropertyChange(
                                        ExtensibilityElementConfigurationEditorComponent.
                                        PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                        "LDAPServerBrowserVisualPanel2.invalid_ldap_server_url"),
                                        jDescriptionDisplayPane);
                }  
                ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                                  "LDAPServerBrowserVisualPanel2.invalid_ldap_server_url"));
                ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
        /*} else if(!conn.validateParam()){
                if (fireEvent) {
                        ErrorPropagator.doFirePropertyChange(
                                        ExtensibilityElementConfigurationEditorComponent.
                                        PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                        "LDAPServerBrowserVisualPanel2.can_not_connect_to_server"),
                                        jDescriptionDisplayPane);
                }  
                ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                                  "LDAPServerBrowserVisualPanel2.can_not_connect_to_server"));
                ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);	*/			
        } else {
            if (fireEvent) {
                    ErrorPropagator.doFirePropertyChange(
                            ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_CLEAR_MESSAGES_EVT, null, "", jDescriptionDisplayPane);            
            }
        }
        return ldapError;
    }

    private LDAPError validateDataFilePath(boolean fireEvent){
        LDAPError ldapError = new LDAPError();
        String dataFile = trimTextField(jTextFieldDataFilePath.getText());
        if (dataFile == null || dataFile.equals("")) {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                        "LDAPServerBrowserVisualPanel2.select_data_ldif_path"),
                        jDescriptionDisplayPane);
            }  
            ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                          "LDAPServerBrowserVisualPanel2.select_data_ldif_path"));
            ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
        } else {   
                File file = new File(dataFile);
                if(!file.exists()){
                        if (fireEvent) {
                                ErrorPropagator.doFirePropertyChange(
                                                ExtensibilityElementConfigurationEditorComponent.
                                                PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                "LDAPServerBrowserVisualPanel2.data_ldif_file_not_exist"),
                                                jDescriptionDisplayPane);
                        }  
                        ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                                          "LDAPServerBrowserVisualPanel2.data_ldif_file_not_exist"));
                        ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
                } else if(!file.isFile()){
                        if (fireEvent) {
                                ErrorPropagator.doFirePropertyChange(
                                                ExtensibilityElementConfigurationEditorComponent.
                                                PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                "LDAPServerBrowserVisualPanel2.select_data_ldif_file_null"),
                                                jDescriptionDisplayPane);
                        }  
                        ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                                          "LDAPServerBrowserVisualPanel2.select_data_ldif_file_null"));
                        ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
                } else if(!dataFile.endsWith(".ldif")){
                                if (fireEvent) {
                                        ErrorPropagator.doFirePropertyChange(
                                                        ExtensibilityElementConfigurationEditorComponent.
                                                        PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                        "LDAPServerBrowserVisualPanel2.data_must_be_ldif_format"),
                                                        jDescriptionDisplayPane);
                                }  
                                ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                                                                                  "LDAPServerBrowserVisualPanel2.data_must_be_ldif_format"));
                                ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
                        }
                else {
                        if (fireEvent) {
                                ErrorPropagator.doFirePropertyChange(
                                        ExtensibilityElementConfigurationEditorComponent.
                                        PROPERTY_CLEAR_MESSAGES_EVT, null, "", jDescriptionDisplayPane);            
                        }
                }
            }
        return ldapError;
    }

	private LDAPError validateSchemaFilePath(boolean fireEvent){
        LDAPError ldapError = new LDAPError();
        String schemaFile = trimTextField(jTextFieldSchemaFilePath.getText());
        if (schemaFile == null || schemaFile.equals("")) {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
                        "LDAPServerBrowserVisualPanel2.select_schema_ldif_path"),
                        jDescriptionDisplayPane);
            }  
            ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
            						  "LDAPServerBrowserVisualPanel2.select_schema_ldif_path"));
            ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
        } else {   
			File file = new File(schemaFile);
			if(!file.exists()){
				if (fireEvent) {
					ErrorPropagator.doFirePropertyChange(
							ExtensibilityElementConfigurationEditorComponent.
							PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
							"LDAPServerBrowserVisualPanel2.schema_ldif_file_not_exist"),
							jDescriptionDisplayPane);
				}  
				ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
										  "LDAPServerBrowserVisualPanel2.schema_ldif_file_not_exist"));
				ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
                        } else if(!file.isFile()){
				if (fireEvent) {
					ErrorPropagator.doFirePropertyChange(
							ExtensibilityElementConfigurationEditorComponent.
							PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
							"LDAPServerBrowserVisualPanel2.select_schema_ldif_file_null"),
							jDescriptionDisplayPane);
				}  
				ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
										  "LDAPServerBrowserVisualPanel2.select_schema_ldif_file_null"));
				ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
                        } else if(!schemaFile.endsWith(".ldif")){
				if (fireEvent) {
					ErrorPropagator.doFirePropertyChange(
							ExtensibilityElementConfigurationEditorComponent.
							PROPERTY_ERROR_EVT, null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
							"LDAPServerBrowserVisualPanel2.schema_must_be_ldif_format"),
							jDescriptionDisplayPane);
				}  
				ldapError.setErrorMessage(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel2.class,
										  "LDAPServerBrowserVisualPanel2.schema_must_be_ldif_format"));
				ldapError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);				
			} else {
                            if (fireEvent) {
                                    ErrorPropagator.doFirePropertyChange(
                                            ExtensibilityElementConfigurationEditorComponent.
                                            PROPERTY_CLEAR_MESSAGES_EVT, null, "", jDescriptionDisplayPane);            
                            }
                        }
                }
        return ldapError;
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonAdvanced;
    private javax.swing.JButton jButtonBrowserDataFile;
    private javax.swing.JButton jButtonBrowserSchemaFile;
    private javax.swing.JTextPane jDescriptionDisplayPane;
    private javax.swing.JScrollPane jDescriptionScrollPane;
    private javax.swing.JLabel jLabelLDAPServerURL;
    private javax.swing.JLabel jLabelLDAPServerURL1;
    private javax.swing.JLabel jLabelRootDN;
    private javax.swing.JLabel jLabelRootDN1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButtonFile;
    private javax.swing.JRadioButton jRadioButtonServer;
    private javax.swing.JTextField jTextFieldDataFilePath;
    private javax.swing.JTextField jTextFieldLDAPServerURL;
    private javax.swing.JTextField jTextFieldRootDN;
    private javax.swing.JTextField jTextFieldSchemaFilePath;
    // End of variables declaration//GEN-END:variables
	// Icon
    private Icon mCASAImg = null;

    // Style Document for Description Area
    private StyledDocument mDoc = null;
    private String[] mStyles = null;

    private static final Logger mLogger = Logger.
            getLogger(LDAPServerBrowserVisualPanel2.class.getName());  
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;
}

