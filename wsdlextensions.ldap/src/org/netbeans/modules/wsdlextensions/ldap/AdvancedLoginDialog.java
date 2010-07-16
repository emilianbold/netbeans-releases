/*
 * AdvancedLoginDialog.java
 *
 * Created on February 12, 2008, 11:17 AM
 */
package org.netbeans.modules.wsdlextensions.ldap;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.wsdlextensions.ldap.utils.LdapConnection;

/**
 *
 * @author  Gary Zheng
 */
public class AdvancedLoginDialog extends javax.swing.JDialog {

    private LdapConnection conn;
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ldap.Bundle");

    /** Creates new form AdvancedLoginDialog */
    /** Creates new form AdvancedLoginDialog */
    public AdvancedLoginDialog(java.awt.Frame parent, boolean modal, LdapConnection conn) {
        super(parent, modal);
        String url = org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class,
        "AdvancedLoginDialog.IMG_CASA");
        Image img = org.openide.util.Utilities.loadImage(url);
		mCASAImg = new ImageIcon(img);        
        initComponents();
        this.conn = conn;
        init();
    }

    public AdvancedLoginDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private void init() {
    	jDescriptionDisplayPane.setEditable(false);
    	jDescriptionDisplayPane.setOpaque(false);
        if (!"".equals(conn.getPrincipal())) {
            this.jTextFieldPrincipal.setText(conn.getPrincipal());
        }
        if (!"".equals(conn.getCredential())) {
            this.jPasswordFieldCredential.setText(conn.getCredential());
        }
        if (!"".equals(conn.getSsltype())) {
            this.jComboBoxSSLType.setSelectedItem(conn.getSsltype());
        }
        if (!"".equals(conn.getAuthentication())) {
            this.jComboBoxAuthentication.setSelectedItem(conn.getAuthentication());
        }
        if (!"".equals(conn.getProtocol())) {
            this.jTextFieldProtocol.setText(conn.getProtocol());
        }
        if (!"".equals(conn.getTruststore())) {
            this.jTextFieldTruststore.setText(conn.getTruststore());
        }
        if (!"".equals(conn.getTruststoretype())) {
            this.jTextFieldTruststoreType.setText(conn.getTruststoretype());
        }
        if (!"".equals(conn.getTruststorepassword())) {
            this.jPasswordFieldTrustStore.setText(conn.getTruststorepassword());
        }
        if (!"".equals(conn.getKeystore())) {
            this.jTextFieldKeystore.setText(conn.getKeystore());
        }
        if (!"".equals(conn.getKeystoretype())) {
            this.jTextFieldKeystoreType.setText(conn.getKeystoretype());
        }
        if (!"".equals(conn.getKeystoreusername())) {
            this.jTextFieldKeystoreUserName.setText(conn.getKeystoreusername());
        }
        if (!"".equals(conn.getKeystorepassword())) {
            this.jPasswordFieldKeyStore.setText(conn.getKeystorepassword());
        }
        if (!"".equals(conn.getKeystorepassword())) {
            jComboBoxTLSSecurity.setSelectedItem(conn.getTlssecurity());
        }
    }

    private String validateInput() {
        String principal = jTextFieldPrincipal.getText();
        String credential = new String(jPasswordFieldCredential.getPassword());
//        String sslType = jTextFieldSSLType.getText();
//        String authentication = jTextFieldAuthentication.getText();
//        String protocol = jTextFieldProtocol.getText();
        String truststore = jTextFieldTruststore.getText();
//        String truststoretype = jTextFieldTruststoreType.getText();
//        String truststorepassword = new String(jPasswordFieldTrustStore.getPassword());
        String keystore = jTextFieldKeystore.getText();
//        String keystoretype = jTextFieldKeystoreType.getText();
//        String keystoreusername = jTextFieldKeystoreUserName.getText();
//        String keystorepassword = new String(jPasswordFieldKeyStore.getPassword());
        String tlssecurity = jComboBoxTLSSecurity.getSelectedItem().toString();
        if ("".equals(principal) && !"".equals(credential) || !"".equals(principal) && "".equals(credential)) {
            return mMessages.getString("AdvancedLoginDialog.not_principal__Credential_consistent");
        }
        if (!"".equals(truststore) && !isFileExist(truststore)) {
            return mMessages.getString("AdvancedLoginDialog.not_truststore_file_exist");
        }
        if (!"".equals(keystore) && !isFileExist(keystore)) {
            return mMessages.getString("AdvancedLoginDialog.not_keystore_file_exist");
        }
        if ("YES".equals(tlssecurity) && "".equals(truststore) && "".equals(keystore)) {
            return mMessages.getString("AdvancedLoginDialog.need_truststore_keystore_when_Tls");
        }
        return "";
    }

    private boolean isFileExist(String filename) {
        File file = new File(filename);
        if (file.isFile()) {
            return true;
        }
        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelPrincipal = new javax.swing.JLabel();
        jLabelCredential = new javax.swing.JLabel();
        jLabelAuthenticationType = new javax.swing.JLabel();
        jLabelProtocol = new javax.swing.JLabel();
        jLabelKeystore = new javax.swing.JLabel();
        jLabelTruststore = new javax.swing.JLabel();
        jTextFieldPrincipal = new javax.swing.JTextField();
        jTextFieldProtocol = new javax.swing.JTextField();
        jTextFieldTruststore = new javax.swing.JTextField();
        jTextFieldKeystore = new javax.swing.JTextField();
        jPasswordFieldCredential = new javax.swing.JPasswordField();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jLabelTrustStorePassword = new javax.swing.JLabel();
        jPasswordFieldTrustStore = new javax.swing.JPasswordField();
        jLabelKeyStorePassword = new javax.swing.JLabel();
        jPasswordFieldKeyStore = new javax.swing.JPasswordField();
        jTextFieldTruststoreType = new javax.swing.JTextField();
        jLabelTruststoreType = new javax.swing.JLabel();
        jTextFieldKeystoreType = new javax.swing.JTextField();
        jTextFieldKeystoreUserName = new javax.swing.JTextField();
        jLabelKeyStoreType = new javax.swing.JLabel();
        jLabelKeystoreUsername = new javax.swing.JLabel();
        jLabelSSLType = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxTLSSecurity = new javax.swing.JComboBox();
        jComboBoxAuthentication = new javax.swing.JComboBox();
        jButtonTruststoreBrowse = new javax.swing.JButton();
        jButtonKeystoreBrowse = new javax.swing.JButton();
        jComboBoxSSLType = new javax.swing.JComboBox();
        jDescriptionScrollPane = new javax.swing.JScrollPane();
        jDescriptionDisplayPane = new javax.swing.JTextPane();
        mDoc = jDescriptionDisplayPane.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};
        Style def = StyleContext.getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = mDoc.addStyle("regular", def);
        Style s = mDoc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.title")); // NOI18N

        jLabelPrincipal.setDisplayedMnemonic('I');
        jLabelPrincipal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelPrincipal.setLabelFor(jTextFieldPrincipal);
        jLabelPrincipal.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelPrincipal.text")); // NOI18N

        jLabelCredential.setDisplayedMnemonic('C');
        jLabelCredential.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelCredential.setLabelFor(jPasswordFieldCredential);
        jLabelCredential.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelCredential.text")); // NOI18N

        jLabelAuthenticationType.setDisplayedMnemonic('H');
        jLabelAuthenticationType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelAuthenticationType.setLabelFor(jComboBoxAuthentication);
        jLabelAuthenticationType.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelAuthenticationType.text")); // NOI18N

        jLabelProtocol.setDisplayedMnemonic('P');
        jLabelProtocol.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelProtocol.setLabelFor(jTextFieldProtocol);
        jLabelProtocol.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelProtocol.text")); // NOI18N

        jLabelKeystore.setDisplayedMnemonic('O');
        jLabelKeystore.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelKeystore.setLabelFor(jTextFieldTruststore);
        jLabelKeystore.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeystore.text")); // NOI18N

        jLabelTruststore.setDisplayedMnemonic('S');
        jLabelTruststore.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelTruststore.setLabelFor(jTextFieldKeystore);
        jLabelTruststore.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelTruststore.text")); // NOI18N

        jTextFieldPrincipal.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldPrincipal.text")); // NOI18N
        jTextFieldPrincipal.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldPrincipal.desc")); // NOI18N
        jTextFieldPrincipal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldPrincipalFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldPrincipalFocusLost(evt);
            }
        });

        jTextFieldProtocol.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldProtocol.text")); // NOI18N
        jTextFieldProtocol.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldProtocol.desc")); // NOI18N
        jTextFieldProtocol.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldProtocolFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldProtocolFocusLost(evt);
            }
        });

        jTextFieldTruststore.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTruststore.text")); // NOI18N
        jTextFieldTruststore.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStore.desc")); // NOI18N
        jTextFieldTruststore.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldTruststoreFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldTruststoreFocusLost(evt);
            }
        });

        jTextFieldKeystore.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeystore.text")); // NOI18N
        jTextFieldKeystore.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStore.desc")); // NOI18N
        jTextFieldKeystore.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldKeystoreFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldKeystoreFocusLost(evt);
            }
        });

        jPasswordFieldCredential.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldCredential.text")); // NOI18N
        jPasswordFieldCredential.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldCredential.desc")); // NOI18N
        jPasswordFieldCredential.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPasswordFieldCredentialFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jPasswordFieldCredentialFocusLost(evt);
            }
        });

        jButtonCancel.setMnemonic('N');
        jButtonCancel.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.setPreferredSize(new java.awt.Dimension(80, 20));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonOK.setMnemonic('K');
        jButtonOK.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonOK.text")); // NOI18N
        jButtonOK.setPreferredSize(new java.awt.Dimension(80, 20));
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jLabelTrustStorePassword.setDisplayedMnemonic('U');
        jLabelTrustStorePassword.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelTrustStorePassword.setLabelFor(jPasswordFieldTrustStore);
        jLabelTrustStorePassword.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelTrustStorePassword.text")); // NOI18N

        jPasswordFieldTrustStore.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldTrustStore.text")); // NOI18N
        jPasswordFieldTrustStore.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldTrustStore.desc")); // NOI18N
        jPasswordFieldTrustStore.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPasswordFieldTrustStoreFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jPasswordFieldTrustStoreFocusLost(evt);
            }
        });

        jLabelKeyStorePassword.setDisplayedMnemonic('R');
        jLabelKeyStorePassword.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelKeyStorePassword.setLabelFor(jPasswordFieldKeyStore);
        jLabelKeyStorePassword.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeyStorePassword.text")); // NOI18N

        jPasswordFieldKeyStore.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldKeyStore.text")); // NOI18N
        jPasswordFieldKeyStore.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldKeyStore.desc")); // NOI18N
        jPasswordFieldKeyStore.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPasswordFieldKeyStoreFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jPasswordFieldKeyStoreFocusLost(evt);
            }
        });

        jTextFieldTruststoreType.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTruststoreType.text")); // NOI18N
        jTextFieldTruststoreType.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStoreType.desc")); // NOI18N
        jTextFieldTruststoreType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldTruststoreTypeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldTruststoreTypeFocusLost(evt);
            }
        });

        jLabelTruststoreType.setDisplayedMnemonic('y');
        jLabelTruststoreType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelTruststoreType.setLabelFor(jTextFieldTruststoreType);
        jLabelTruststoreType.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelTruststoreType.text")); // NOI18N

        jTextFieldKeystoreType.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeystoreType.text")); // NOI18N
        jTextFieldKeystoreType.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreType.desc")); // NOI18N
        jTextFieldKeystoreType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldKeystoreTypeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldKeystoreTypeFocusLost(evt);
            }
        });

        jTextFieldKeystoreUserName.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeystoreUserName.text")); // NOI18N
        jTextFieldKeystoreUserName.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreUserName.desc")); // NOI18N
        jTextFieldKeystoreUserName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldKeystoreUserNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldKeystoreUserNameFocusLost(evt);
            }
        });

        jLabelKeyStoreType.setDisplayedMnemonic('E');
        jLabelKeyStoreType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelKeyStoreType.setLabelFor(jTextFieldKeystoreType);
        jLabelKeyStoreType.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeyStoreType.text")); // NOI18N

        jLabelKeystoreUsername.setDisplayedMnemonic('M');
        jLabelKeystoreUsername.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelKeystoreUsername.setLabelFor(jTextFieldKeystoreUserName);
        jLabelKeystoreUsername.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeystoreUsername.text")); // NOI18N

        jLabelSSLType.setDisplayedMnemonic('T');
        jLabelSSLType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelSSLType.setLabelFor(jComboBoxSSLType);
        jLabelSSLType.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelSSLType.text")); // NOI18N

        jLabel1.setDisplayedMnemonic('L');
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setLabelFor(jComboBoxTLSSecurity);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabel1.text")); // NOI18N

        jComboBoxTLSSecurity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NO", "YES" }));
        jComboBoxTLSSecurity.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxTLSSecurity.desc")); // NOI18N
        jComboBoxTLSSecurity.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBoxTLSSecurityFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jComboBoxTLSSecurityFocusLost(evt);
            }
        });

        jComboBoxAuthentication.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "simple", "strong" }));
        jComboBoxAuthentication.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxAuthentication.desc")); // NOI18N
        jComboBoxAuthentication.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBoxAuthenticationFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jComboBoxAuthenticationFocusLost(evt);
            }
        });

        jButtonTruststoreBrowse.setMnemonic('W');
        jButtonTruststoreBrowse.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonTruststoreBrowse.text")); // NOI18N
        jButtonTruststoreBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTruststoreBrowseActionPerformed(evt);
            }
        });

        jButtonKeystoreBrowse.setMnemonic('B');
        jButtonKeystoreBrowse.setText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonKeystoreBrowse.text")); // NOI18N
        jButtonKeystoreBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonKeystoreBrowseActionPerformed(evt);
            }
        });

        jComboBoxSSLType.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"None", "Enable SSL", "TLS on demand" }));
        jComboBoxSSLType.setToolTipText(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxSSLType.desc")); // NOI18N
        jComboBoxSSLType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBoxSSLTypeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jComboBoxSSLTypeFocusLost(evt);
            }
        });

        jDescriptionScrollPane.setName("jDescriptionScrollPane"); // NOI18N

        jDescriptionDisplayPane.setBackground(new java.awt.Color(240, 240, 240));
        jDescriptionDisplayPane.setName("jDescriptionDisplayPane"); // NOI18N
        jDescriptionScrollPane.setViewportView(jDescriptionDisplayPane);
        jDescriptionDisplayPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jDescriptionDisplayPane.AccessibleContext.accessibleName")); // NOI18N
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jDescriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(jLabelAuthenticationType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                            .add(jLabelProtocol, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelSSLType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelCredential, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelPrincipal, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .add(jLabelKeyStorePassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelKeystoreUsername, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelKeyStoreType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelTruststore, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelTrustStorePassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelTruststoreType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelKeystore))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jComboBoxSSLType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jComboBoxTLSSecurity, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPasswordFieldKeyStore)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldKeystoreUserName)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldKeystoreType)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldKeystore)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPasswordFieldTrustStore)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldTruststoreType)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldTruststore)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldProtocol)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jComboBoxAuthentication, 0, 187, Short.MAX_VALUE))
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jTextFieldPrincipal)
                                    .add(jPasswordFieldCredential, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jButtonKeystoreBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jButtonTruststoreBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(33, 33, 33))))
            .add(layout.createSequentialGroup()
                .add(104, 104, 104)
                .add(jButtonOK, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jButtonCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(150, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(jTextFieldPrincipal)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPasswordFieldCredential, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabelPrincipal)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelCredential, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelSSLType)
                    .add(jComboBoxSSLType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxAuthentication, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelAuthenticationType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldProtocol, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelProtocol, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldTruststore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelKeystore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonTruststoreBrowse))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldTruststoreType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelTruststoreType))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jPasswordFieldTrustStore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelTrustStorePassword))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldKeystore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelTruststore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonKeystoreBrowse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldKeystoreType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelKeyStoreType))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldKeystoreUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelKeystoreUsername))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jPasswordFieldKeyStore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelKeyStorePassword))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxTLSSecurity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jDescriptionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonOK, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11))
        );

        jLabelPrincipal.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelPrincipal.desc")); // NOI18N
        jLabelCredential.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelCredential.desc")); // NOI18N
        jLabelAuthenticationType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelAuthenticationType.desc")); // NOI18N
        jLabelProtocol.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelProtocol.desc")); // NOI18N
        jLabelKeystore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeyStore.desc")); // NOI18N
        jLabelTruststore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelTrustStore.desc")); // NOI18N
        jTextFieldPrincipal.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldPrincipal.name")); // NOI18N
        jTextFieldPrincipal.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldPrincipal.desc")); // NOI18N
        jTextFieldProtocol.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldProtocol.name")); // NOI18N
        jTextFieldProtocol.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldProtocol.desc")); // NOI18N
        jTextFieldTruststore.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStore.name")); // NOI18N
        jTextFieldTruststore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStore.desc")); // NOI18N
        jTextFieldKeystore.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStore.name")); // NOI18N
        jTextFieldKeystore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStore.desc")); // NOI18N
        jPasswordFieldCredential.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldCredential.name")); // NOI18N
        jPasswordFieldCredential.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldCredential.desc")); // NOI18N
        jButtonCancel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonCancel.name")); // NOI18N
        jButtonCancel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonCancel.desc")); // NOI18N
        jButtonOK.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonOk.name")); // NOI18N
        jButtonOK.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonOk.desc")); // NOI18N
        jLabelTrustStorePassword.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelTrustStorePassword.desc")); // NOI18N
        jPasswordFieldTrustStore.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldTrustStore.name")); // NOI18N
        jPasswordFieldTrustStore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldTrustStore.desc")); // NOI18N
        jLabelKeyStorePassword.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeyStorePassword.desc")); // NOI18N
        jPasswordFieldKeyStore.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldKeyStore.name")); // NOI18N
        jPasswordFieldKeyStore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldKeyStore.desc")); // NOI18N
        jTextFieldTruststoreType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStoreType.name")); // NOI18N
        jTextFieldTruststoreType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStoreType.desc")); // NOI18N
        jLabelTruststoreType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelTrustStoreType.desc")); // NOI18N
        jTextFieldKeystoreType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreType.name")); // NOI18N
        jTextFieldKeystoreType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreType.desc")); // NOI18N
        jTextFieldKeystoreUserName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreUserName.name")); // NOI18N
        jTextFieldKeystoreUserName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreUserName.desc")); // NOI18N
        jLabelKeyStoreType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeyStoreType.desc")); // NOI18N
        jLabelKeystoreUsername.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelKeyStoreUserName.desc")); // NOI18N
        jLabelSSLType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabelSSLType.desc")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jLabel1.desc")); // NOI18N
        jComboBoxTLSSecurity.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxTLSSecurity.name")); // NOI18N
        jComboBoxTLSSecurity.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxTLSSecurity.desc")); // NOI18N
        jComboBoxAuthentication.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxAuthentication.name")); // NOI18N
        jComboBoxAuthentication.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxAuthentication.desc")); // NOI18N
        jButtonTruststoreBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonTruststoreBrowse.AccessibleContext.accessibleName")); // NOI18N
        jButtonTruststoreBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonTrustStoreBrowse.desc")); // NOI18N
        jButtonKeystoreBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonKeystoreBrowse.AccessibleContext.accessibleName")); // NOI18N
        jButtonKeystoreBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jButtonKeyStoreBrowse.desc")); // NOI18N
        jComboBoxSSLType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxSSLType.name")); // NOI18N
        jComboBoxSSLType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxSSLType.desc")); // NOI18N
        jDescriptionScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jDescriptionScrollPane.AccessibleContext.accessibleName")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.desc")); // NOI18N

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        // TODO add your handling code here:
        String validate = validateInput();
        if (!"".equals(validate)) {
            JOptionPane.showMessageDialog(null, validate, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!conn.getPrincipal().equals(this.jTextFieldPrincipal.getText().trim()) ||
                !conn.getCredential().equals(new String(this.jPasswordFieldCredential.getPassword())) ||
                !conn.getSsltype().equals(jComboBoxSSLType.getSelectedItem().toString()) ||
                !conn.getAuthentication().equals(jComboBoxAuthentication.getSelectedItem().toString()) ||
                !conn.getProtocol().equals(this.jTextFieldProtocol.getText().trim()) ||
                !conn.getTruststore().equals(this.jTextFieldTruststore.getText().trim()) ||
                !conn.getTruststoretype().equals(jTextFieldTruststoreType.getText().trim()) ||
                !conn.getTruststorepassword().equals(new String(this.jPasswordFieldTrustStore.getPassword())) ||
                !conn.getKeystore().equals(this.jTextFieldKeystore.getText().trim()) ||
                !conn.getKeystoretype().equals(this.jTextFieldKeystoreType.getText().trim()) ||
                !conn.getKeystoreusername().equals(this.jTextFieldKeystoreUserName.getText().trim()) ||
                !conn.getKeystorepassword().equals(new String(this.jPasswordFieldKeyStore.getPassword())) ||
                !conn.getTlssecurity().equals(this.jComboBoxTLSSecurity.getSelectedItem().toString())) {
            conn.setConnectionReconnect(true);
        }
        conn.setPrincipal(this.jTextFieldPrincipal.getText().trim());
        conn.setCredential(new String(this.jPasswordFieldCredential.getPassword()));
        conn.setSsltype(this.jComboBoxSSLType.getSelectedItem().toString());
        conn.setAuthentication(this.jComboBoxAuthentication.getSelectedItem().toString());
        conn.setProtocol(this.jTextFieldProtocol.getText().trim());
        conn.setTruststore(this.jTextFieldTruststore.getText().trim());
        conn.setTruststoretype(this.jTextFieldTruststoreType.getText().trim());
        conn.setTruststorepassword(new String(this.jPasswordFieldTrustStore.getPassword()));
        conn.setKeystore(this.jTextFieldKeystore.getText().trim());
        conn.setKeystoretype(this.jTextFieldKeystoreType.getText().trim());
        conn.setKeystoreusername(this.jTextFieldKeystoreUserName.getText().trim());
        conn.setKeystorepassword(new String(this.jPasswordFieldKeyStore.getPassword()));
        conn.setTlssecurity(this.jComboBoxTLSSecurity.getSelectedItem().toString());
        this.dispose();
}//GEN-LAST:event_jButtonOKActionPerformed

private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
    // TODO add your handling code here:
    this.dispose();
}//GEN-LAST:event_jButtonCancelActionPerformed

private void jButtonTruststoreBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTruststoreBrowseActionPerformed
// TODO add your handling code here:
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Please select trust store file");

    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    String path = jTextFieldTruststore.getText();
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
        jTextFieldTruststore.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}//GEN-LAST:event_jButtonTruststoreBrowseActionPerformed

private void jButtonKeystoreBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonKeystoreBrowseActionPerformed
// TODO add your handling code here:
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Please select key store file");

    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    String path = jTextFieldKeystore.getText();
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
        jTextFieldKeystore.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}//GEN-LAST:event_jButtonKeystoreBrowseActionPerformed

private void jTextFieldPrincipalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPrincipalFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldPrincipalFocusGained

private void jTextFieldPrincipalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPrincipalFocusLost
    clearDescription();
}//GEN-LAST:event_jTextFieldPrincipalFocusLost

private void jPasswordFieldCredentialFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFieldCredentialFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jPasswordFieldCredentialFocusGained

private void jPasswordFieldCredentialFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFieldCredentialFocusLost
    clearDescription();
}//GEN-LAST:event_jPasswordFieldCredentialFocusLost

private void jComboBoxSSLTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxSSLTypeFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jComboBoxSSLTypeFocusGained

private void jComboBoxSSLTypeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxSSLTypeFocusLost
    clearDescription();
}//GEN-LAST:event_jComboBoxSSLTypeFocusLost

private void jComboBoxAuthenticationFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxAuthenticationFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jComboBoxAuthenticationFocusGained

private void jComboBoxAuthenticationFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxAuthenticationFocusLost
    clearDescription();
}//GEN-LAST:event_jComboBoxAuthenticationFocusLost

private void jTextFieldProtocolFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldProtocolFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldProtocolFocusGained

private void jTextFieldProtocolFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldProtocolFocusLost
    clearDescription();
}//GEN-LAST:event_jTextFieldProtocolFocusLost

private void jTextFieldTruststoreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTruststoreFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldTruststoreFocusGained

private void jTextFieldTruststoreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTruststoreFocusLost
    clearDescription();
}//GEN-LAST:event_jTextFieldTruststoreFocusLost

private void jTextFieldTruststoreTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTruststoreTypeFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldTruststoreTypeFocusGained

private void jTextFieldTruststoreTypeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTruststoreTypeFocusLost
    clearDescription();
}//GEN-LAST:event_jTextFieldTruststoreTypeFocusLost

private void jPasswordFieldTrustStoreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFieldTrustStoreFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jPasswordFieldTrustStoreFocusGained

private void jPasswordFieldTrustStoreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFieldTrustStoreFocusLost
    clearDescription();
}//GEN-LAST:event_jPasswordFieldTrustStoreFocusLost

private void jTextFieldKeystoreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldKeystoreFocusGained

private void jTextFieldKeystoreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreFocusLost
    clearDescription();
}//GEN-LAST:event_jTextFieldKeystoreFocusLost

private void jTextFieldKeystoreTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreTypeFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldKeystoreTypeFocusGained

private void jTextFieldKeystoreTypeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreTypeFocusLost
    clearDescription();
}//GEN-LAST:event_jTextFieldKeystoreTypeFocusLost

private void jTextFieldKeystoreUserNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreUserNameFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jTextFieldKeystoreUserNameFocusGained

private void jTextFieldKeystoreUserNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreUserNameFocusLost
    clearDescription();
}//GEN-LAST:event_jTextFieldKeystoreUserNameFocusLost

private void jPasswordFieldKeyStoreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFieldKeyStoreFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jPasswordFieldKeyStoreFocusGained

private void jPasswordFieldKeyStoreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFieldKeyStoreFocusLost
    clearDescription();
}//GEN-LAST:event_jPasswordFieldKeyStoreFocusLost

private void jComboBoxTLSSecurityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxTLSSecurityFocusGained
	updateDescription(evt);
}//GEN-LAST:event_jComboBoxTLSSecurityFocusGained

private void jComboBoxTLSSecurityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxTLSSecurityFocusLost
    clearDescription();
}//GEN-LAST:event_jComboBoxTLSSecurityFocusLost

private void clearDescription() {
    jDescriptionDisplayPane.setText("");
}

private void updateDescription(FocusEvent evt) {
	jDescriptionDisplayPane.setText("");
	// The image must first be wrapped in a style
	Style style =  mDoc.addStyle("StyleName", null);
	StyleConstants.setIcon(style, mCASAImg);
	String[] desc = null;
	boolean casaEdited = false;

    if (evt.getSource() == jTextFieldPrincipal) {
        desc = new String[]{" Principal ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldPrincipal.desc")};
        casaEdited = true;
    } else if (evt.getSource() == jPasswordFieldCredential){
        desc = new String[]{" Credential ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldCredential.desc")};
        casaEdited = true;
    } else if (evt.getSource() == jComboBoxSSLType){
        desc = new String[]{" SSL Connection Type ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxSSLType.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jComboBoxAuthentication){
        desc = new String[]{" Authentication Type",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxAuthentication.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jTextFieldProtocol){
        desc = new String[]{" Protocal ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldProtocol.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jTextFieldTruststore){
        desc = new String[]{" TrustStore ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStore.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jTextFieldTruststoreType){
        desc = new String[]{" TrustStore Type ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldTrustStoreType.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jPasswordFieldTrustStore){
        desc = new String[]{" TrustStore Password ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldTrustStore.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jTextFieldKeystore){
        desc = new String[]{" KeyStore ",
               org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStore.desc")};
         casaEdited = true;
    }  else if (evt.getSource() == jTextFieldKeystoreType){
        desc = new String[]{" KeyStore Type ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreType.desc")};
         casaEdited = true;
    }  else if (evt.getSource() == jTextFieldKeystoreUserName){
        desc = new String[]{" KeyStore User Name ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jTextFieldKeyStoreUserName.desc")};
         casaEdited = true;
    }  else if (evt.getSource() == jPasswordFieldKeyStore){
        desc = new String[]{" KeyStore Password ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jPasswordFieldKeyStore.desc")};
         casaEdited = true;
    } else if (evt.getSource() == jComboBoxTLSSecurity){
        desc = new String[]{" TLS Security ",
                org.openide.util.NbBundle.getMessage(AdvancedLoginDialog.class, "AdvancedLoginDialog.jComboBoxTLSSecurity.desc")};
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
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                AdvancedLoginDialog dialog = new AdvancedLoginDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonKeystoreBrowse;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonTruststoreBrowse;
    private javax.swing.JComboBox jComboBoxAuthentication;
    private javax.swing.JComboBox jComboBoxSSLType;
    private javax.swing.JComboBox jComboBoxTLSSecurity;
    private javax.swing.JTextPane jDescriptionDisplayPane;
    private javax.swing.JScrollPane jDescriptionScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelAuthenticationType;
    private javax.swing.JLabel jLabelCredential;
    private javax.swing.JLabel jLabelKeyStorePassword;
    private javax.swing.JLabel jLabelKeyStoreType;
    private javax.swing.JLabel jLabelKeystore;
    private javax.swing.JLabel jLabelKeystoreUsername;
    private javax.swing.JLabel jLabelPrincipal;
    private javax.swing.JLabel jLabelProtocol;
    private javax.swing.JLabel jLabelSSLType;
    private javax.swing.JLabel jLabelTrustStorePassword;
    private javax.swing.JLabel jLabelTruststore;
    private javax.swing.JLabel jLabelTruststoreType;
    private javax.swing.JPasswordField jPasswordFieldCredential;
    private javax.swing.JPasswordField jPasswordFieldKeyStore;
    private javax.swing.JPasswordField jPasswordFieldTrustStore;
    private javax.swing.JTextField jTextFieldKeystore;
    private javax.swing.JTextField jTextFieldKeystoreType;
    private javax.swing.JTextField jTextFieldKeystoreUserName;
    private javax.swing.JTextField jTextFieldPrincipal;
    private javax.swing.JTextField jTextFieldProtocol;
    private javax.swing.JTextField jTextFieldTruststore;
    private javax.swing.JTextField jTextFieldTruststoreType;
    // End of variables declaration//GEN-END:variables

	// Icon
    private Icon mCASAImg = null;

    // Style Document for Description Area
    private StyledDocument mDoc = null;
    private String[] mStyles = null;

    private static final Logger mLogger = Logger.
            getLogger(AdvancedLoginDialog.class.getName());    
}
