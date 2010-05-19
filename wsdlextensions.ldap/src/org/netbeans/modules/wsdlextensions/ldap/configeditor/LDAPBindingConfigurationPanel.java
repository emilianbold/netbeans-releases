/*
 * LDAPBindingConfigurationPanel.java
 *
 * Created on August 8, 2008, 2:51 PM
 */

package org.netbeans.modules.wsdlextensions.ldap.configeditor;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.ldap.LDAPAddress;
import org.netbeans.modules.wsdlextensions.ldap.LDAPBinding;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperation;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperationInput;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperationOutput;
import org.netbeans.modules.wsdlextensions.ldap.validator.LDAPComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;

/**
 *
 * @author  admin
 */
public class LDAPBindingConfigurationPanel extends javax.swing.JPanel {

    
     /** the WSDL model to configure **/
    private WSDLComponent mComponent;
    
     /** QName **/
    private QName mQName;
    
    /** error buffer **/
    private StringBuffer mErrBuff = null;
    
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.ldap.resources.Bundle");

     /** style document for description area **/
    private StyledDocument mDoc = null;
    private StyledDocument mDocAdv = null;
    private StyledDocument mDocAdvOut = null;
    private String[] mStyles = null;

    /** error buffer **/
    private Icon mCASAImg = null;
    /** Creates new form LDAPBindingConfigurationPanel */
    public LDAPBindingConfigurationPanel() {
        initComponents();
    }
    
     public LDAPBindingConfigurationPanel(QName qName, WSDLComponent component) {
        mComponent = component;
        mQName = qName;
        initComponents();
        resetView();
        populateView(mComponent);
    }

        private PortType getPortType(String bindingName, LDAPAddress ldapAddress) {
        if ((ldapAddress != null) && (ldapAddress.getParent() != null)) {
            Port parentPort = (Port) ldapAddress.getParent();
            Service parentService = (Service) parentPort.getParent();
            Definitions defs = (Definitions) parentService.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<LDAPBinding> ldapBindings = null;
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }
                NamedComponentReference<PortType> portType = binding.getType();
                if (binding.getName().equals(bindingName)) {
                    return portType.get();
                }
            }
        }
        return null;
    }

         /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
        // do LDAPBC-specific validation first

        boolean ok = validateMe();
        if (!ok) {
            return ok;
        }

        ValidationResult results = new LDAPComponentValidator().
                validate(mComponent.getModel(), null, ValidationType.COMPLETE);
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
            firePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, null);
            return true;
        }

    }

    public boolean validateMe() {
        boolean ok = true;
        String ldaplocation = getLDAPLocation();
        if ((ldaplocation == null) ||(ldaplocation.equals(""))) {
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    LDAPBindingConfigurationPanel.class,
                    "LDAPBindingConfigurationPanel.LDAPLocationMustBeSet"));
            return false;
        }
       /* 
        String truststorelocation = trustStoreTextField.getText();
        if(!truststorelocation.endsWith(".jks") || !truststorelocation.equals("")){
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    LDAPBindingConfigurationPanel.class,
                    "LDAPBindingConfigurationPanel.TrustStoreLocationInvalid"));
            return false;
        }*/
     return ok;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        button1 = new java.awt.Button();
        jFileChooser1 = new javax.swing.JFileChooser();
        jFileChooser2 = new javax.swing.JFileChooser();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        jPanel1 = new javax.swing.JPanel();
        BindingConfigurationPanel = new javax.swing.JTabbedPane();
        generalTabPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        LDAPLocationTextField = new javax.swing.JTextField();
        principalTextField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        partNametextField = new javax.swing.JTextField();
        CredentialsPassWordField = new javax.swing.JPasswordField();
        protocolComboBox = new javax.swing.JComboBox();
        advancedTabPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        trustStoreTextField = new javax.swing.JTextField();
        trustStoreTypeTextField = new javax.swing.JTextField();
        keyStoreTextField = new javax.swing.JTextField();
        keyStoreTypeTextField = new javax.swing.JTextField();
        keyStoreUserNameTextField = new javax.swing.JTextField();
        BrowseButton = new javax.swing.JButton();
        BrowseButtonForKeyStore = new javax.swing.JButton();
        authenticationComboBox = new javax.swing.JComboBox();
        sslTypeComboBox = new javax.swing.JComboBox();
        trustStorePasswordField = new javax.swing.JPasswordField();
        keyStorePasswordField = new javax.swing.JPasswordField();
        jScrollPane1 = new javax.swing.JScrollPane();
        ToolTipTextDisplayTextPane = new javax.swing.JTextPane();
        mDoc = ToolTipTextDisplayTextPane.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};
        Style def = StyleContext.getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = mDoc.addStyle("regular", def);
        Style s = mDoc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        bindingNameComboBox = new javax.swing.JComboBox();
        portTypeComboBox = new javax.swing.JComboBox();
        servicePortComboBox = new javax.swing.JComboBox();
        operationNameComboBox = new javax.swing.JComboBox();

        button1.setLabel(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.button1.label")); // NOI18N
        button1.setName("button1"); // NOI18N

        jFileChooser1.setName("jFileChooser1"); // NOI18N

        jFileChooser2.setName("jFileChooser2"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        jPopupMenu2.setName("jPopupMenu2"); // NOI18N

        setName("Form"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        BindingConfigurationPanel.setName("BindingConfigurationPanel"); // NOI18N

        generalTabPanel.setName("generalTabPanel"); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setLabelFor(LDAPLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setLabelFor(protocolComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setLabelFor(principalTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setLabelFor(CredentialsPassWordField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        LDAPLocationTextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.LDAPLocationTextField.text")); // NOI18N
        LDAPLocationTextField.setName("LDAPLocationTextField"); // NOI18N
        LDAPLocationTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LDAPLocationTextFieldActionPerformed(evt);
            }
        });
        LDAPLocationTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                LDAPLocationTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                LDAPLocationTextFieldFocusLost(evt);
            }
        });

        principalTextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.principalTextField.text")); // NOI18N
        principalTextField.setName("principalTextField"); // NOI18N
        principalTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                principalTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                principalTextFieldFocusLost(evt);
            }
        });

        jLabel17.setLabelFor(partNametextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        partNametextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.partNametextField.text")); // NOI18N
        partNametextField.setName("partNametextField"); // NOI18N
        partNametextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                partNametextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                partNametextFieldFocusLost(evt);
            }
        });

        CredentialsPassWordField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.CredentialsPassWordField.text")); // NOI18N
        CredentialsPassWordField.setName("CredentialsPassWordField"); // NOI18N
        CredentialsPassWordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                CredentialsPassWordFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                CredentialsPassWordFieldFocusLost(evt);
            }
        });

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2" }));
        protocolComboBox.setName("protocolComboBox"); // NOI18N
        protocolComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                protocolComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                protocolComboBoxFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout generalTabPanelLayout = new org.jdesktop.layout.GroupLayout(generalTabPanel);
        generalTabPanel.setLayout(generalTabPanelLayout);
        generalTabPanelLayout.setHorizontalGroup(
            generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalTabPanelLayout.createSequentialGroup()
                .add(61, 61, 61)
                .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jLabel2)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(jLabel17))
                .add(74, 74, 74)
                .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(partNametextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(protocolComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(LDAPLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                        .add(principalTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .add(CredentialsPassWordField, 0, 0, Short.MAX_VALUE)))
                .addContainerGap(110, Short.MAX_VALUE))
            .add(generalTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(268, Short.MAX_VALUE))
        );
        generalTabPanelLayout.setVerticalGroup(
            generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(18, 18, 18)
                .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(LDAPLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(38, 38, 38)
                .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(protocolComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(35, 35, 35)
                .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(principalTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .add(31, 31, 31)
                .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(CredentialsPassWordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .add(34, 34, 34)
                .add(generalTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(partNametextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel17))
                .add(108, 108, 108))
        );

        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel2.name")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel3.desc")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel4.desc")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel5.desc")); // NOI18N
        LDAPLocationTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.LDAPLocationTextField.name")); // NOI18N
        LDAPLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.LDAPLocationTextField.desc")); // NOI18N
        principalTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.principalTextField.name")); // NOI18N
        principalTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.principalTextField.desc")); // NOI18N
        jLabel17.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel7.desc")); // NOI18N
        partNametextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.partNametextField.name")); // NOI18N
        partNametextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.partNametextField.desc")); // NOI18N
        CredentialsPassWordField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.CredentialPassWordField.name")); // NOI18N
        CredentialsPassWordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.CredentialsPassWordField.desc")); // NOI18N
        protocolComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.protocolComboBox.name")); // NOI18N
        protocolComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.protocolComboBox.desc")); // NOI18N

        BindingConfigurationPanel.addTab(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.generalTabPanel.TabConstraints.tabTitle"), generalTabPanel); // NOI18N

        advancedTabPanel.setName("advancedTabPanel"); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel6.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setLabelFor(sslTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setLabelFor(authenticationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setLabelFor(trustStoreTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setLabelFor(trustStorePasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel11.setLabelFor(trustStoreTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel12.setLabelFor(keyStoreTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setLabelFor(keyStoreTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jLabel14.setLabelFor(keyStoreUserNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jLabel15.setLabelFor(keyStorePasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        trustStoreTextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStoreTextField.text")); // NOI18N
        trustStoreTextField.setToolTipText(getTrustStoreProperty());
        trustStoreTextField.setMaximumSize(new java.awt.Dimension(10, 20));
        trustStoreTextField.setName("trustStoreTextField"); // NOI18N
        trustStoreTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                trustStoreTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                trustStoreTextFieldFocusLost(evt);
            }
        });

        trustStoreTypeTextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStoreTypeTextField.text")); // NOI18N
        trustStoreTypeTextField.setName("trustStoreTypeTextField"); // NOI18N
        trustStoreTypeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                trustStoreTypeTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                trustStoreTypeTextFieldFocusLost(evt);
            }
        });

        keyStoreTextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreTextField.text")); // NOI18N
        keyStoreTextField.setName("keyStoreTextField"); // NOI18N
        keyStoreTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyStoreTextFieldActionPerformed(evt);
            }
        });
        keyStoreTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyStoreTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyStoreTextFieldFocusLost(evt);
            }
        });

        keyStoreTypeTextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreTypeTextField.text")); // NOI18N
        keyStoreTypeTextField.setName("keyStoreTypeTextField"); // NOI18N
        keyStoreTypeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyStoreTypeTextFieldActionPerformed(evt);
            }
        });
        keyStoreTypeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyStoreTypeTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyStoreTypeTextFieldFocusLost(evt);
            }
        });

        keyStoreUserNameTextField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreUserNameTextField.text")); // NOI18N
        keyStoreUserNameTextField.setName("keyStoreUserNameTextField"); // NOI18N
        keyStoreUserNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyStoreUserNameTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyStoreUserNameTextFieldFocusLost(evt);
            }
        });

        BrowseButton.setMnemonic('W');
        BrowseButton.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.BrowseButton.text")); // NOI18N
        BrowseButton.setName("BrowseButton"); // NOI18N
        BrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseButtonActionPerformed(evt);
            }
        });

        BrowseButtonForKeyStore.setMnemonic('B');
        BrowseButtonForKeyStore.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.BrowseButtonForKeyStore.text")); // NOI18N
        BrowseButtonForKeyStore.setName("BrowseButtonForKeyStore"); // NOI18N
        BrowseButtonForKeyStore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseButtonForKeyStoreActionPerformed(evt);
            }
        });

        authenticationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        authenticationComboBox.setName("authenticationComboBox"); // NOI18N
        authenticationComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                authenticationComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                authenticationComboBoxFocusLost(evt);
            }
        });

        sslTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3" }));
        sslTypeComboBox.setName("sslTypeComboBox"); // NOI18N
        sslTypeComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sslTypeComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                sslTypeComboBoxFocusLost(evt);
            }
        });

        trustStorePasswordField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStorePasswordField.text")); // NOI18N
        trustStorePasswordField.setName("trustStorePasswordField"); // NOI18N
        trustStorePasswordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                trustStorePasswordFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                trustStorePasswordFieldFocusLost(evt);
            }
        });

        keyStorePasswordField.setText(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStorePasswordField.text")); // NOI18N
        keyStorePasswordField.setName("keyStorePasswordField"); // NOI18N
        keyStorePasswordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyStorePasswordFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyStorePasswordFieldFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout advancedTabPanelLayout = new org.jdesktop.layout.GroupLayout(advancedTabPanel);
        advancedTabPanel.setLayout(advancedTabPanelLayout);
        advancedTabPanelLayout.setHorizontalGroup(
            advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedTabPanelLayout.createSequentialGroup()
                .add(51, 51, 51)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(jLabel12)
                    .add(jLabel15)
                    .add(jLabel14)
                    .add(jLabel13)
                    .add(jLabel11)
                    .add(jLabel8)
                    .add(jLabel9)
                    .add(jLabel10))
                .add(48, 48, 48)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(keyStorePasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(keyStoreUserNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(trustStoreTypeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(keyStoreTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(trustStoreTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(authenticationComboBox, 0, 124, Short.MAX_VALUE)
                    .add(keyStoreTypeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(sslTypeComboBox, 0, 124, Short.MAX_VALUE)
                    .add(trustStorePasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, BrowseButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                    .add(BrowseButtonForKeyStore, 0, 0, Short.MAX_VALUE))
                .add(30, 30, 30))
            .add(advancedTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addContainerGap(305, Short.MAX_VALUE))
        );
        advancedTabPanelLayout.setVerticalGroup(
            advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedTabPanelLayout.createSequentialGroup()
                .add(4, 4, 4)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(sslTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 9, Short.MAX_VALUE)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(advancedTabPanelLayout.createSequentialGroup()
                        .add(jLabel8)
                        .add(18, 18, 18)
                        .add(jLabel9)
                        .add(52, 52, 52))
                    .add(advancedTabPanelLayout.createSequentialGroup()
                        .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(BrowseButton)
                            .add(advancedTabPanelLayout.createSequentialGroup()
                                .add(authenticationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(9, 9, 9)
                                .add(trustStoreTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(15, 15, 15)
                        .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(trustStorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)))
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(trustStoreTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 20, Short.MAX_VALUE)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel12)
                    .add(keyStoreTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(BrowseButtonForKeyStore))
                .add(27, 27, 27)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(keyStoreTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(27, 27, 27)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(keyStoreUserNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(22, 22, 22)
                .add(advancedTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel15)
                    .add(keyStorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.jLabel6.desc")); // NOI18N
        trustStoreTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStoreTextField.name")); // NOI18N
        trustStoreTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStoreTextField.desc")); // NOI18N
        trustStoreTypeTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStoreTypeTextField.name")); // NOI18N
        trustStoreTypeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStoreTypeTextField.desc")); // NOI18N
        keyStoreTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreTextField.name")); // NOI18N
        keyStoreTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreTextField.desc")); // NOI18N
        keyStoreTypeTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreTypeTextField.name")); // NOI18N
        keyStoreTypeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreTypeTextField.desc")); // NOI18N
        keyStoreUserNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreUserNameTextField.name")); // NOI18N
        keyStoreUserNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStoreUserNameTextField.desc")); // NOI18N
        BrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.BrowseButton.desc")); // NOI18N
        BrowseButtonForKeyStore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.BroseButtonForKeyStore.desc")); // NOI18N
        authenticationComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.authenticationComboBox.name")); // NOI18N
        authenticationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.authenticationComboBox.desc")); // NOI18N
        sslTypeComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.sslTypeComboBox.name")); // NOI18N
        trustStorePasswordField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStorePasswordField.name")); // NOI18N
        trustStorePasswordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.trustStorePasswordField.desc")); // NOI18N
        keyStorePasswordField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStorePasswordField.name")); // NOI18N
        keyStorePasswordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.keyStorePasswordField.desc")); // NOI18N

        BindingConfigurationPanel.addTab(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.advancedTabPanel.TabConstraints.tabTitle"), advancedTabPanel); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        ToolTipTextDisplayTextPane.setEditable(false);
        ToolTipTextDisplayTextPane.setName("ToolTipTextDisplayTextPane"); // NOI18N
        jScrollPane1.setViewportView(ToolTipTextDisplayTextPane);
        ToolTipTextDisplayTextPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.TooltipTextDisplayTextPane.name")); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N
        bindingNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bindingNameComboBoxItemStateChanged(evt);
            }
        });
        bindingNameComboBox.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                bindingNameComboBoxComponentHidden(evt);
            }
        });

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        portTypeComboBox.setName("portTypeComboBox"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                    .add(BindingConfigurationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 476, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bindingNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, portTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, servicePortComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, operationNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(bindingNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(45, 45, 45)
                        .add(portTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(47, 47, 47)
                        .add(servicePortComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(35, 35, 35)
                        .add(operationNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(180, 180, 180))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(BindingConfigurationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 409, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
        );

        BindingConfigurationPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPBindingConfigurationPanel.class, "LDAPBindingConfigurationPanel.JTabbedPane.General.name")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 475, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void LDAPLocationTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_LDAPLocationTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_LDAPLocationTextFieldFocusGained

private void LDAPLocationTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_LDAPLocationTextFieldFocusLost
// TODO add your handling code here:
     clearToolTipTextArea();
}//GEN-LAST:event_LDAPLocationTextFieldFocusLost

private void bindingNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bindingNameComboBoxItemStateChanged
// TODO add your handling code here:
     if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String selectedBinding = (String) bindingNameComboBox.
                getSelectedItem();
        // if binding name is changed, update the selected port type
        if (mComponent != null)  {
            if (mComponent instanceof LDAPAddress) {
                PortType portType = getPortType(selectedBinding,
                        (LDAPAddress) mComponent);
                 portTypeComboBox.setSelectedItem(portType.getName());
            } else if (mComponent instanceof LDAPBinding) {
                Binding parentBinding = (Binding)
                        ((LDAPBinding) mComponent).getParent();
                NamedComponentReference<PortType> portType =
                        parentBinding.getType();
                if ((portType != null) && (portType.get() != null)) {
                    portTypeComboBox.setSelectedItem(portType.get().getName());
                }
            } else if (mComponent instanceof LDAPOperationInput) {
                Object obj = ((LDAPOperationInput)mComponent).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } 
                if ((parentBinding != null) &&
                        (parentBinding.getType() != null) &&
                        (parentBinding.getType().get() == null)) {                                                    
                    NamedComponentReference<PortType> portType =
                            parentBinding.getType();
                    if (parentBinding.getName().equals(selectedBinding)) {
                         portTypeComboBox.setSelectedItem(portType.get().getName());
                    }
                }
            } else if (mComponent instanceof LDAPOperationOutput) {
                Object obj = ((LDAPOperationOutput)mComponent).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingOutput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } 
                if ((parentBinding != null) &&
                        (parentBinding.getType() != null) &&
                        (parentBinding.getType().get() == null)) {                                                    
                    NamedComponentReference<PortType> portType =
                            parentBinding.getType();
                    if (parentBinding.getName().equals(selectedBinding)) {
                         portTypeComboBox.setSelectedItem(portType.get().getName());
                    }
                }
            }else if (mComponent instanceof LDAPOperation) {
                Object obj = ((LDAPOperation)mComponent).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding)
                            ((BindingOperation)obj).getParent();
                    Collection<LDAPBinding> bindings = parentBinding.
                            getExtensibilityElements(LDAPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateLDAPBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.setSelectedItem(
                                parentBinding.getName());
                    }
                }
            }
        }
    }
}//GEN-LAST:event_bindingNameComboBoxItemStateChanged

private void servicePortComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_servicePortComboBoxItemStateChanged
// TODO add your handling code here:
     if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {                                                    
        Object selObj = servicePortComboBox.getSelectedItem();
        String selBindingName = "";
        if (bindingNameComboBox.getSelectedItem() != null) {
            selBindingName = bindingNameComboBox.getSelectedItem().toString();
        }
        if ((selObj != null) && (mComponent != null)) {
            Port selServicePort = (Port) selObj;
            if (selServicePort.getBinding() != null) {
                Binding binding = selServicePort.getBinding().get();
                if ((binding != null) && (binding.getName().
                        equals(selBindingName))) {
                    Iterator<LDAPAddress> ldapAddresses = selServicePort.getExtensibilityElements(LDAPAddress.class).iterator();
                    // 1 fileaddress for 1 binding
                    while (ldapAddresses.hasNext()) {
                        updateServiceView(ldapAddresses.next());
                        break;
                    }
                }
            }
        }
    }
}//GEN-LAST:event_servicePortComboBoxItemStateChanged

private void operationNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_operationNameComboBoxItemStateChanged
// TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String selectedOperation = (String) operationNameComboBox.
                getSelectedItem();
        if (mComponent != null)  {
            Binding binding = null;
            if (mComponent instanceof LDAPAddress) {
                Port port = (Port) ((LDAPAddress) mComponent).getParent();
                binding = port.getBinding().get();
            } else if (mComponent instanceof LDAPBinding) {
                binding = (Binding) ((LDAPBinding) mComponent).getParent();
           
            } else if (mComponent instanceof LDAPOperationInput) {
                Object obj = ((LDAPOperationInput)mComponent).getParent();
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    binding = (Binding) parentOp.getParent();
                }
                
            }else if (mComponent instanceof LDAPOperationOutput) {
                Object obj = ((LDAPOperationOutput)mComponent).getParent();
                if (obj instanceof BindingOutput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingOutput) obj).getParent();
                    binding = (Binding) parentOp.getParent();
                }
                
            } else if (mComponent instanceof LDAPOperation) {
                Object obj = ((LDAPOperation)mComponent).getParent();
                if (obj instanceof BindingOperation) {
                    binding = (Binding) ((BindingOperation)obj).getParent();
                }
            }
            if (binding != null) {
                              
                LDAPOperationInput inputMessage = getInputLDAPOperationInput(binding,
                        selectedOperation);
                updateInputMessageView(inputMessage);
                LDAPOperationOutput outputMessage = getOutputLDAPOperationOutput(binding,
                        selectedOperation);
                updateOutputMessageView(outputMessage);
                if (outputMessage == null) {
                    //updateOutputMessageViewFromInput(inputMessage);
                }
            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged

private void bindingNameComboBoxComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_bindingNameComboBoxComponentHidden
// TODO add your handling code here:
}//GEN-LAST:event_bindingNameComboBoxComponentHidden

private void LDAPLocationTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LDAPLocationTextFieldActionPerformed
// TODO add your handling code here:
     if ((LDAPLocationTextField.getText() == null) ||
            LDAPLocationTextField.getText().equals("")) {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT, null,
                NbBundle.getMessage(LDAPBindingConfigurationPanel.class,
                "LDAPBindingConfigurationPanel.LDAPLOCATION_EMPTY"));
    } else {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");
    }
}//GEN-LAST:event_LDAPLocationTextFieldActionPerformed

private void keyStoreTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyStoreTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_keyStoreTextFieldActionPerformed

private void keyStoreTypeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyStoreTypeTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_keyStoreTypeTextFieldActionPerformed


private void principalTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_principalTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_principalTextFieldFocusGained

private void principalTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_principalTextFieldFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_principalTextFieldFocusLost

private void partNametextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_partNametextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_partNametextFieldFocusGained

private void partNametextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_partNametextFieldFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_partNametextFieldFocusLost

private void trustStoreTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStoreTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_trustStoreTextFieldFocusGained

private void trustStoreTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStoreTextFieldFocusLost
// TODO add your handling code here:
     clearToolTipTextArea();
}//GEN-LAST:event_trustStoreTextFieldFocusLost

private void trustStoreTypeTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStoreTypeTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_trustStoreTypeTextFieldFocusGained

private void trustStoreTypeTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStoreTypeTextFieldFocusLost
// TODO add your handling code here:
     clearToolTipTextArea();
}//GEN-LAST:event_trustStoreTypeTextFieldFocusLost

private void keyStoreTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyStoreTextFieldFocusGained

private void keyStoreTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreTextFieldFocusLost
// TODO add your handling code here:
     clearToolTipTextArea();
}//GEN-LAST:event_keyStoreTextFieldFocusLost

private void keyStoreTypeTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreTypeTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyStoreTypeTextFieldFocusGained

private void keyStoreTypeTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreTypeTextFieldFocusLost
// TODO add your handling code here:
     clearToolTipTextArea();
}//GEN-LAST:event_keyStoreTypeTextFieldFocusLost

private void keyStoreUserNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreUserNameTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyStoreUserNameTextFieldFocusGained

private void keyStoreUserNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreUserNameTextFieldFocusLost
// TODO add your handling code here:
     clearToolTipTextArea();
}//GEN-LAST:event_keyStoreUserNameTextFieldFocusLost

private void CredentialsPassWordFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_CredentialsPassWordFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_CredentialsPassWordFieldFocusGained

private void CredentialsPassWordFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_CredentialsPassWordFieldFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_CredentialsPassWordFieldFocusLost

private void BrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseButtonActionPerformed
// TODO add your handling code here:
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            int retVal = jFileChooser1.showDialog(null, "Select");
            if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                trustStoreTextField.setText(jFileChooser1.
                        getSelectedFile().getAbsolutePath());
                if(trustStoreTextField.getText().endsWith(".jks")){
                    trustStoreTypeTextField.setText("JKS");
                }
                // per BC developer, no need to validate directory
                //validateFilePath(getDirectoryTextField().getText());
            }
        }
    });
}//GEN-LAST:event_BrowseButtonActionPerformed

private void BrowseButtonForKeyStoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseButtonForKeyStoreActionPerformed
// TODO add your handling code here:
        SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            int retVal = jFileChooser2.showDialog(null, "Select");
            if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                keyStoreTextField.setText(jFileChooser2.
                        getSelectedFile().getAbsolutePath());
                if(keyStoreTextField.getText().endsWith(".jks")){
                    keyStoreTypeTextField.setText("JKS");
                }else if(keyStoreTextField.getText().endsWith(".pkcs12")){
                    keyStoreTypeTextField.setText("PKCS12");
                }
                // per BC developer, no need to validate directory
                //validateFilePath(getDirectoryTextField().getText());
            }
        }
    });
}//GEN-LAST:event_BrowseButtonForKeyStoreActionPerformed

private void trustStorePasswordFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStorePasswordFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_trustStorePasswordFieldFocusGained

private void trustStorePasswordFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStorePasswordFieldFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_trustStorePasswordFieldFocusLost

private void keyStorePasswordFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStorePasswordFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyStorePasswordFieldFocusGained

private void keyStorePasswordFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStorePasswordFieldFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_keyStorePasswordFieldFocusLost

private void protocolComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_protocolComboBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_protocolComboBoxFocusGained

private void protocolComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_protocolComboBoxFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_protocolComboBoxFocusLost

private void sslTypeComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sslTypeComboBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_sslTypeComboBoxFocusGained

private void sslTypeComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sslTypeComboBoxFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_sslTypeComboBoxFocusLost

private void authenticationComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_authenticationComboBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_authenticationComboBoxFocusGained

private void authenticationComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_authenticationComboBoxFocusLost
// TODO add your handling code here:
    clearToolTipTextArea();
}//GEN-LAST:event_authenticationComboBoxFocusLost



    private void updateInputMessageView(LDAPOperationInput inputMessage) {
        //throw new UnsupportedOperationException("Not yet implemented");
        //operationTypeComboBox.setSelectedItem(inputMessage.getAttribute(LDAPOperationInput.LDAP_OPERATIONTYPE_PROPERTY));
        //operationTypeComboBox.setToolTipText(mBundle.getString("DESC_OPERATIONTYPE"));
    }

    private void updateOperation(LDAPOperation ldapoperation) {
        //throw new UnsupportedOperationException("Not yet implemented");
         
 
    }

    private void updateOutputMessageView(LDAPOperationOutput outputMessage) {
         if (outputMessage != null) {
            partNametextField.setText(outputMessage.getAttribute(LDAPOperationOutput.LDAP_RETPARTNAME_PROPERTY));
            partNametextField.setToolTipText(mBundle.getString("DESC_RETURN_PARTNAME"));
            
            //AttributesTextField.setText(outputMessage.getAttribute(LDAPOperationOutput.LDAP_ATTRIBUTES_PROPERTY));
            //AttributesTextField.setToolTipText(mBundle.getString("DESC_ATTRIBUTES"));
            
         }
        //throw new UnsupportedOperationException("Not yet implemented");
    }
    public class PortCellRenderer extends JLabel
            implements javax.swing.ListCellRenderer {

        public PortCellRenderer() {
            super();
            setOpaque(true);
        }

        public Component getListCellRendererComponent(javax.swing.JList list,
                Object value, int index, boolean isSelected,
                boolean isFocused) {
            if ((value != null) && (value instanceof Port)) {
                setText(((Port) value).getName());
                setBackground(isSelected ?
                    list.getSelectionBackground() : list.getBackground());
                setForeground(isSelected ?
                    list.getSelectionForeground() : list.getForeground());
            }
            return this;
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane BindingConfigurationPanel;
    private javax.swing.JButton BrowseButton;
    private javax.swing.JButton BrowseButtonForKeyStore;
    private javax.swing.JPasswordField CredentialsPassWordField;
    private javax.swing.JTextField LDAPLocationTextField;
    private javax.swing.JTextPane ToolTipTextDisplayTextPane;
    private javax.swing.JPanel advancedTabPanel;
    private javax.swing.JComboBox authenticationComboBox;
    private javax.swing.JComboBox bindingNameComboBox;
    private java.awt.Button button1;
    private javax.swing.JPanel generalTabPanel;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFileChooser jFileChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPasswordField keyStorePasswordField;
    private javax.swing.JTextField keyStoreTextField;
    private javax.swing.JTextField keyStoreTypeTextField;
    private javax.swing.JTextField keyStoreUserNameTextField;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JTextField partNametextField;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JTextField principalTextField;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JComboBox sslTypeComboBox;
    private javax.swing.JPasswordField trustStorePasswordField;
    private javax.swing.JTextField trustStoreTextField;
    private javax.swing.JTextField trustStoreTypeTextField;
    // End of variables declaration//GEN-END:variables

   private LDAPOperationInput getInputLDAPOperationInput(Binding binding,
            String selectedOperation) {
        LDAPOperationInput inputFileMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingInput bi = bop.getBindingInput();
                    List<LDAPOperationInput> inputFileMessages =
                            bi.getExtensibilityElements(LDAPOperationInput.class);
                    if (inputFileMessages.size() > 0) {
                        inputFileMessage = inputFileMessages.get(0);
                        break;
                    }
                }
            }
        }
        return inputFileMessage;
    }

    private LDAPOperationOutput getOutputLDAPOperationOutput(Binding binding,
            String selectedOperation) {
        LDAPOperationOutput outputFileMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<LDAPOperationOutput> outputFileMessages =
                                bo.getExtensibilityElements(LDAPOperationOutput.class);
                        if (outputFileMessages.size() > 0) {
                            outputFileMessage = outputFileMessages.get(0);
                            break;
                        }
                    }
                }
            }

        }
        return outputFileMessage;
    }

     private LDAPOperation getLDAPOperation(Binding binding,
            String selectedOperation) {
        LDAPOperation ldapoperation = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    if(bop != null){
                        List<LDAPOperation> ldapoperations =
                                bop.getExtensibilityElements(LDAPOperation.class);
                        if(ldapoperations.size()>0){
                            ldapoperation = ldapoperations.get(0);
                            break;
                        }
                    }
                    }
            }

        }
        return ldapoperation;
    }
    private void populateDescriptionAndTooltip() {
        //throw new UnsupportedOperationException("Not yet implemented");
    }
    // End of variables declaration

    private void populateLDAPBinding(LDAPBinding ldapBinding, LDAPAddress ldapAddress) {
        if (ldapAddress == null) {
            servicePortComboBox.setEnabled(true);
            ldapAddress = getLDAPAddress(ldapBinding);
        }
        if (ldapAddress == null) {
            return;
        }
        Port port = (Port) ldapAddress.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(ldapBinding);
        servicePortComboBox.setSelectedItem(port);

        // from Binding, need to allow changing of Port
        bindingNameComboBox.setEditable(false);
        bindingNameComboBox.setEnabled(false);

        updateServiceView(ldapAddress);
        if (ldapBinding != null) {
            populateListOfBindings(ldapBinding);
            populateListOfPortTypes(ldapBinding);
            Binding binding = (Binding) ldapBinding.getParent();
            bindingNameComboBox.setSelectedItem(binding.getName());
            NamedComponentReference<PortType> pType = binding.getType();
            PortType portType = pType.get();
            portTypeComboBox.setSelectedItem(portType.getName());

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            populateOperations(bindingOperations);

            operationNameComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    // based on selected operation, populate messages
                    operationNameComboBoxItemStateChanged(evt);
                }
            });
            // select the 1st item since this is not a configurable param
            operationNameComboBox.setSelectedIndex(0);
            if (operationNameComboBox.getItemCount() == 1) {
                // need to implicitly call update on messages because above
                // listener will not change selection if only 1 item
                if (binding != null) {
                    LDAPOperationInput inputMessage = getInputLDAPOperationInput(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    //updateInputMessageView(inputMessage);

                    LDAPOperationOutput outputMessage = getOutputLDAPOperationOutput(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateOutputMessageView(outputMessage);
                    if (outputMessage == null) {
                        //updateOutputMessageViewFromInput(inputMessage);
                    }
                }
            }

        }

    }
    // End of variables declaration

    private void resetView() {
        //TODO
        String url = NbBundle.getMessage(LDAPBindingConfigurationPanel.class,
                "LDAPBindingConfigurationPanel.IMG_CASA");
        Image img = org.openide.util.Utilities.loadImage(url);
        mCASAImg = new ImageIcon(img);
        servicePortComboBox.setEnabled(false);
        portTypeComboBox.setEditable(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
        protocolComboBox.removeAllItems();
        protocolComboBox.addItem("none");
        protocolComboBox.addItem("ssl");
        sslTypeComboBox.removeAllItems();
        sslTypeComboBox.addItem("None");
        sslTypeComboBox.addItem("Enable SSL");
        sslTypeComboBox.addItem("TLS on demand");
        authenticationComboBox.removeAllItems();
        authenticationComboBox.addItem("none");
        authenticationComboBox.addItem("simple");
        authenticationComboBox.addItem("strong");
        //operationTypeComboBox.removeAllItems();
        //operationTypeComboBox.addItem("searchRequest");
        //operationTypeComboBox.addItem("updateRequest");
        //operationTypeComboBox.addItem("compareRequest");
        //operationTypeComboBox.addItem("insertRequest");
        //operationTypeComboBox.addItem("deleteRequest");
        //operationTypeComboBox.addItem("addConnectionRequest");
        
        if (mErrBuff == null) {
            mErrBuff = new StringBuffer();
        }
    }
     /**
     * Return the service port
     * @return String service port
     */
    Port getServicePort() {
        return (Port) servicePortComboBox.getSelectedItem();
    }
     /**
     * Return the binding name used
     * @return String binding name
     */
    String getBinding() {
        if ((bindingNameComboBox.getSelectedItem() != null) &&
                (!bindingNameComboBox.getSelectedItem().toString().
                equals("<Not Set>"))) {
            return bindingNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }
    /**
     * Return the port type used
     * @return String port type name
     */
    String getPortType() {
        if ((portTypeComboBox.getSelectedItem() != null) &&
                (!portTypeComboBox.getSelectedItem().toString().
                equals("<Not Set>"))) {
            return portTypeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }
   
     /**
     * Return the operation name
     * @return String operation name
     */
    String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals("<Not Set>"))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }
    
    
   /* String getOperationType() {
        if ((operationTypeComboBox.getSelectedItem() != null) &&
                (!operationTypeComboBox.getSelectedItem().toString().
                equals("<Not Set>"))) {
            return operationTypeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }*/
    
    void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
        }
    }
    
    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof LDAPAddress) {
                populateLDAPAddress((LDAPAddress) component);
            } else if (component instanceof LDAPBinding) {
                populateLDAPBinding((LDAPBinding) component, null);
            } else if (component instanceof Port) {
                Collection<LDAPAddress> address = ((Port) component).
                        getExtensibilityElements(LDAPAddress.class);
                if (!address.isEmpty()) {
                    populateLDAPAddress(address.iterator().next());
                }
            } else if (component instanceof LDAPOperationInput) {
                Object obj = ((LDAPOperationInput)component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } 
                if (parentBinding != null) {
                    Collection<LDAPBinding> bindings = parentBinding.
                            getExtensibilityElements(LDAPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateLDAPBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.
                                setSelectedItem(parentBinding.getName());
                    }
                }
            } else if (component instanceof LDAPOperationOutput) {
                Object obj = ((LDAPOperationOutput)component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingOutput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } 
                if (parentBinding != null) {
                    Collection<LDAPBinding> bindings = parentBinding.
                            getExtensibilityElements(LDAPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateLDAPBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.
                                setSelectedItem(parentBinding.getName());
                    }
                }
            } else if (component instanceof LDAPOperation) {
                Object obj = ((LDAPOperation)component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding)
                            ((BindingOperation)obj).getParent();
                    Collection<LDAPBinding> bindings = parentBinding.
                            getExtensibilityElements(LDAPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateLDAPBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.setSelectedItem(
                                parentBinding.getName());
                    }
                }
            }
            populateDescriptionAndTooltip();
        }
            //populateDescriptionAndTooltip();
    }
    String getLDAPLocation() {
       String ldaplocation = LDAPLocationTextField.getText();
       if ((ldaplocation != null) && (ldaplocation.equals(""))) {
            return null;
        }
        return ldaplocation;
    }
    
     String getCredentials(){
         //String  credentials = credentialsTextField.getText();
         String credentials = String.valueOf(CredentialsPassWordField.getPassword());
        if ((credentials != null) && (credentials.equals(""))) {
            return null;
        }
        return credentials;
    }
    
     String getProtocol(){
         String  protocol = protocolComboBox.getSelectedItem().toString();
        if ((protocol != null) && (protocol.equals(""))) {
            return null;
        }
        return protocol;
    }
     
    String getLDAPPrincipalProperty(){
        String Principal = principalTextField.getText();
        if ((Principal != null) && (Principal.equals(""))) {
            return null;
        }
        return Principal;
        
    }
    
    String getsslTypeProperty(){
        String ssltype = sslTypeComboBox.getSelectedItem().toString();
        if ((ssltype != null) && (ssltype.equals(""))) {
            return null;
        }
        return ssltype;
        
    }
    
    String getAuthenticationProperty(){
        String authentication = authenticationComboBox.getSelectedItem().toString();
        if ((authentication != null) && (authentication.equals(""))) {
            return null;
        }
        return authentication;
     }
    
    String getTrustStoreProperty(){
        String truststore = trustStoreTextField.getText();
        if ((truststore != null) && (truststore.equals(""))) {
            return null;
        }
        return truststore;
     }
    
    String getTrustStorePasswordProperty(){
        String truststorepassword = String.valueOf(trustStorePasswordField.getPassword());
        if ((truststorepassword != null) && (truststorepassword.equals(""))) {
            return null;
        }
        return truststorepassword;
     }
    
     String getTrustStoreTypeProperty(){
        String truststoretype = trustStoreTypeTextField.getText();
        if ((truststoretype != null) && (truststoretype.equals(""))) {
            return null;
        }
        return truststoretype;
     }
     
     String getKeyStoreProperty(){
        String keystore = keyStoreTextField.getText();
        if ((keystore != null) && (keystore.equals(""))) {
            return null;
        }
        return keystore;
     }
     
     String getKeyStoreTypeProperty(){
        String keystoretype = keyStoreTypeTextField.getText();
        if ((keystoretype != null) && (keystoretype.equals(""))) {
            return null;
        }
        return keystoretype;
     }
     
      String getKeyStoreUserNameProperty(){
        String keystoreusername = keyStoreUserNameTextField.getText();
        if ((keystoreusername != null) && (keystoreusername.equals(""))) {
            return null;
        }
        return keystoreusername;
     }
     
     String getKeyStorePasswordProperty(){
        String keystorepassword = String.valueOf(keyStorePasswordField.getPassword());
        if ((keystorepassword != null) && (keystorepassword.equals(""))) {
            return null;
        }
        return keystorepassword;
     }
      
     String getOutputPartName(){
        String outputpartname = partNametextField.getText();
        if ((outputpartname != null) && (outputpartname.equals(""))) {
            return null;
        }
        return outputpartname;
     }
      
   /*  String getAttributes(){
        String attributes = AttributesTextField.getText();
        if ((attributes != null) && (attributes.equals(""))) {
            return null;
        }
        return attributes;
     }*/
     
   
    
    private void locationFieldActionPerformed(java.awt.event.ActionEvent evt) {                                                   
//    validateFilePath(getDirectoryTextField().getText());                                                  
    if ((LDAPLocationTextField.getText() == null) ||
            LDAPLocationTextField.getText().equals("")) {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT, null,
                NbBundle.getMessage(LDAPBindingConfigurationPanel.class,
                "LDAPBindingConfigurationPanel.LDAPLOCATION_EMPTY"));
    } else {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");
    }
}  
    
private void updateServiceView(LDAPAddress ldapAddress) {
        if (ldapAddress != null) {
            LDAPLocationTextField.setText(ldapAddress.
                    getAttribute(LDAPAddress.LDAP_LOCATION_PROPERTY));
            LDAPLocationTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_location")); //NOI18N
            jLabel2.setToolTipText(
                    mBundle.getString("DESC_Attribute_location")); //NOI18N
            protocolComboBox.setSelectedItem(ldapAddress.getAttribute(LDAPAddress.LDAP_PROTOCOL_PROPERTY));
            protocolComboBox.setToolTipText(mBundle.getString("DESC_PROTOCOL_PROPERTY"));
            
            principalTextField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_PRINCIPAL_PROPERTY));
            principalTextField.setToolTipText(mBundle.getString("DESC_PRINCIPAL_PROPERTY"));
            
            CredentialsPassWordField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_CREDENTIAL_PROPERTY));
            principalTextField.setToolTipText(mBundle.getString("DESC_CREDENTIAL_PROPERTY"));
            
            sslTypeComboBox.setSelectedItem(ldapAddress.getAttribute(LDAPAddress.LDAP_SSLTYPE_PROPERTY));
            sslTypeComboBox.setToolTipText(mBundle.getString("DESC_SSL_TYPE"));
            
            authenticationComboBox.setSelectedItem(ldapAddress.getAttribute(LDAPAddress.LDAP_AUTHENTICATION_PROPERTY));
            authenticationComboBox.setToolTipText(mBundle.getString("DESC_AUTHENTICATION"));
            
            trustStoreTextField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_TRUSTSTORE_PROPERTY));
            trustStoreTextField.setToolTipText(mBundle.getString("DESC_TRUSTSTORE"));
            
            trustStoreTypeTextField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_TRUSTSTORETYPE_PROPERTY));
            trustStoreTypeTextField.setToolTipText(mBundle.getString("DESC_TRUSTSTORE_TYPE"));
            
            trustStorePasswordField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_TRUSTSTOREPASSWORD_PROPERTY));
            trustStorePasswordField.setToolTipText(mBundle.getString("DESC_TRUSTSTORE_PASSWORD"));
            
            keyStoreTextField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_KEYSTORE_PROPERTY));
            keyStoreTextField.setToolTipText(mBundle.getString("DESC_KEYSTORE"));
            
            keyStoreTypeTextField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_KEYSTORETYPE_PROPERTY));
            keyStoreTypeTextField.setToolTipText(mBundle.getString("DESC_KEYSTORETYPE"));
           
            keyStorePasswordField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_KEYSTOREPASSWORD_PROPERTY));
            keyStorePasswordField.setToolTipText(mBundle.getString("DESC_KEYSTORE_USERNAME"));
            
            keyStoreUserNameTextField.setText(ldapAddress.getAttribute(LDAPAddress.LDAP_KEYSTOREUSERNAME_PROPERTY));
            keyStoreUserNameTextField.setToolTipText(mBundle.getString("DESC_KEYSTORE_PASSWORD"));
            
            
        }
    }

private void updateDescriptionArea(FocusEvent evt) {
        ToolTipTextDisplayTextPane.setText("");
        
        // The image must first be wrapped in a style
        Style style =  mDoc.addStyle("StyleName", null);
        StyleConstants.setIcon(style, mCASAImg);
        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == LDAPLocationTextField) {
            desc = new String[]{"LDAP Server Location ",
                    mBundle.getString("DESC_Attribute_location")};
            casaEdited = true;
        } else if (evt.getSource() == protocolComboBox){
            desc = new String[]{" Protocol  ",
                    mBundle.getString("DESC_PROTOCOL_PROPERTY")};
            casaEdited = true;
        } else if (evt.getSource() == principalTextField){
            desc = new String[]{" Principal  ",
                    mBundle.getString("DESC_PRINCIPAL_PROPERTY")};
             casaEdited = true;
        } else if (evt.getSource() == CredentialsPassWordField){
            desc = new String[]{" Credentials  ",
                    mBundle.getString("DESC_CREDENTIAL_PROPERTY")};
             casaEdited = true;
        } else if (evt.getSource() == partNametextField){
            desc = new String[]{" Return Part Name  ",
                    mBundle.getString("DESC_Attribute_returnPartName")};
             casaEdited = true;
        //} else if (evt.getSource() == AttributesTextField){
          //  desc = new String[]{" Attributes  ",
            //        mBundle.getString("DESC_Attribute_attributes")};
            // casaEdited = true;
        } else if (evt.getSource() == sslTypeComboBox){
            desc = new String[]{" SSL Type  ",
                    mBundle.getString("DESC_SSL_TYPE")};
             casaEdited = true;
        } else if (evt.getSource() == authenticationComboBox){
            desc = new String[]{" Authentication Type  ",
                    mBundle.getString("DESC_AUTHENTICATION")};
             casaEdited = true;
        } else if (evt.getSource() == trustStoreTextField){
            desc = new String[]{" Truststore  ",
                    mBundle.getString("DESC_TRUSTSTORE")};
             casaEdited = true;
        } else if (evt.getSource() == trustStorePasswordField){
            desc = new String[]{" Truststore Password  ",
                    mBundle.getString("DESC_TRUSTSTORE_PASSWORD")};
             casaEdited = true;
        }  else if (evt.getSource() == trustStoreTypeTextField){
            desc = new String[]{" Truststore Type  ",
                    mBundle.getString("DESC_TRUSTSTORE_TYPE")};
             casaEdited = true;
        }  else if (evt.getSource() == keyStoreTextField){
            desc = new String[]{" Keystore  ",
                    mBundle.getString("DESC_KEYSTORE")};
             casaEdited = true;
        }  else if (evt.getSource() == keyStoreTypeTextField){
            desc = new String[]{" Keystore Type  ",
                    mBundle.getString("DESC_KEYSTORETYPE")};
             casaEdited = true;
        } else if (evt.getSource() == keyStoreUserNameTextField){
            desc = new String[]{" Keystore Username  ",
                    mBundle.getString("DESC_KEYSTORE_USERNAME")};
             casaEdited = true;
        } else if (evt.getSource() == keyStorePasswordField){
            desc = new String[]{" Keystore Password  ",
                    mBundle.getString("DESC_KEYSTORE_PASSWORD")};
             casaEdited = true;
        }
        if (desc != null) {
            try {
                ToolTipTextDisplayTextPane.getStyledDocument().insertString(mDoc.getLength(), desc[0],
                        mDoc.getStyle(mStyles[0]));
                mDoc.insertString(mDoc.getLength(), desc[1],
                        mDoc.getStyle(mStyles[1]));

           // Insert the image
             /*   if (casaEdited) {
                    mDoc.insertString(mDoc.getLength(), "\n",
                            mDoc.getStyle(mStyles[1]));
                    mDoc.insertString(mDoc.getLength(), "ignored text", style);
                    mDoc.insertString(mDoc.getLength(), "  " + NbBundle.
                            getMessage(LDAPBindingConfigurationPanel.class,
                            "LDAPBindingConfigurationPanel.CASA_EDITED"),
                            mDoc.getStyle(mStyles[1]));
                }*/
     
                ToolTipTextDisplayTextPane.setCaretPosition(0);
            } catch(BadLocationException ble) {
            }
            return;
        }
    
}


    private void clearToolTipTextArea() {
        ToolTipTextDisplayTextPane.setText("");
    }
 
   
    private void populateLDAPAddress(LDAPAddress ldapAddress) {
        Port port = (Port) ldapAddress.getParent();
        if (port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            Collection<LDAPBinding> bindings = binding.
                    getExtensibilityElements(LDAPBinding.class);
            servicePortComboBox.setEnabled(false);
            servicePortComboBox.setSelectedItem(port.getName());
            if (!bindings.isEmpty()) {
                populateLDAPBinding(bindings.iterator().next(), ldapAddress);
                bindingNameComboBox.setSelectedItem(binding.getName());
            }
            // from Port, need to disable binding box as 1:1 relationship
            bindingNameComboBox.setEditable(false);
            bindingNameComboBox.setEnabled(false);
        }
    }
    
     LDAPAddress getLDAPAddressPerSelectedPort() {
        LDAPAddress address = null;
        Port selectedServicePort = (Port) servicePortComboBox.getSelectedItem();
        if (selectedServicePort != null) {
            Binding binding = selectedServicePort.getBinding().get();
            String selBindingName = bindingNameComboBox.
                    getSelectedItem().toString();
            if ((binding != null) && (binding.getName().
                    equals(selBindingName))) {
                Iterator<LDAPAddress> ldapAddresses = selectedServicePort.
                        getExtensibilityElements(LDAPAddress.class).
                        iterator();
                // 1 fileaddress for 1 binding
                while (ldapAddresses.hasNext()) {
                    return ldapAddresses.next();
                }
            }
        }
        return address;
    }
       LDAPAddress getLDAPAddress(LDAPBinding ldapBinding) {
        LDAPAddress ldapAddress = null;
        if ((ldapBinding != null) && (ldapBinding.getParent() != null)) {
            Binding parentBinding = (Binding) ldapBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            Iterator<LDAPAddress> ldapAddresses = port.
                                    getExtensibilityElements(LDAPAddress.class).
                                    iterator();
                            // 1 fileaddress for 1 binding
                            while (ldapAddresses.hasNext()) {
                                return ldapAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return ldapAddress;
    } 
      private void populateListOfPorts(LDAPBinding ldapBinding) {
            Vector<Port> portV = new Vector<Port>();

        if ((ldapBinding != null) && (ldapBinding.getParent() != null)) {
            Binding parentBinding = (Binding) ldapBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            portV.add(port);
                        }
                    }
                }
            }
        }
        servicePortComboBox.setModel(new DefaultComboBoxModel(portV));
        servicePortComboBox.setRenderer(new PortCellRenderer());

    }
    private void populateOperations(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            //updateOperation((LDAPOperation)bop);
            operationNameComboBox.addItem(bop.getName());
        }
    }
    private void populateListOfBindings(LDAPBinding ldapBinding) {
        if ((ldapBinding != null) && (ldapBinding.getParent() != null)) {
            Binding parentBinding = (Binding) ldapBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<LDAPBinding> ldapBindings = null;

            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }

                ldapBindings = binding.
                        getExtensibilityElements(LDAPBinding.class);
                if (ldapBindings != null) {
                    Iterator iter = ldapBindings.iterator();
                    while (iter.hasNext()) {
                        LDAPBinding b = (LDAPBinding) iter.next();
                        Binding fBinding = (Binding) b.getParent();
                        bindingNameComboBox.addItem(fBinding.getName());
                    }
                }
            }
        }
    }
   private void populateListOfPortTypes(LDAPBinding fileBinding) {
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<PortType> portTypes = defs.getPortTypes().iterator();
            List<PortType> filePortTypes = null;
            while (portTypes.hasNext()) {
                PortType portType = portTypes.next();
                portTypeComboBox.addItem(portType.getName());
            }
        }
    }
   
}
