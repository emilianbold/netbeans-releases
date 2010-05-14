/*
 * FTPSettingsRequestResponseMessagePanel.java
 *
 * Created on September 22, 2008, 10:33 AM
 */
package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.ftp.FTPAddress;
import org.netbeans.modules.wsdlextensions.ftp.FTPBinding;
import org.netbeans.modules.wsdlextensions.ftp.FTPConstants;
import org.netbeans.modules.wsdlextensions.ftp.FTPMessage;
import org.netbeans.modules.wsdlextensions.ftp.FTPOperation;
import org.netbeans.modules.wsdlextensions.ftp.FTPTransfer;
import org.netbeans.modules.wsdlextensions.ftp.validator.FTPAddressURL;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author  jfu
 */
public class FTPSettingsRequestResponseMessagePanel extends javax.swing.JPanel implements AncestorListener, PropertyAccessible, BindingConfigurationDelegate {

    /** the WSDL model to configure **/
    private WSDLComponent mWsdlComponent;
    /** QName **/
    private QName mQName;
    /**
     * Project associated with this wsdl
     */
    private Project mProject = null;
    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.ftp.resources.Bundle");
    private static final Logger mLogger = Logger.getLogger(FTPSettingsRequestResponseMessagePanel.class.getName());
    private DescriptionPanel descriptionPanel = null;
    private MyItemListener mItemListener = null;
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JComboBox operationNameComboBox;
    private ChangeListener mTabChangeListener;
    private PropertyChangeSupport mProxy;

    /** Creates new form FTPSettingsRequestResponseMessagePanel */
    public FTPSettingsRequestResponseMessagePanel(QName qName, WSDLComponent component) {
        this(qName, component, null);
    }

    public FTPSettingsRequestResponseMessagePanel(QName qName, WSDLComponent component, PropertyChangeSupport proxy) {
        mProxy = proxy;
        initComponents();
        initCustomComponents();
        populateView(qName, component);
    }

    public void setProject(Project project) {
        mProject = project;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class,
                "FTPSettingsRequestResponseMessagePanel.StepLabel");
    }

    public void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
        }
    }

    /**
     * Return the operation name
     * @return String operation name
     */
    public String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals(FTPConstants.NOT_SET))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    public void setCorrelate(boolean correlateEnabled) {
        messageCorrelateLab.setSelected(correlateEnabled);
    }

    private boolean commitAddress(FTPAddress fTPAddress) {
        WSDLModel wsdlModel = fTPAddress.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }

            String passwd = new String(passwdFld.getPassword());
            String passwdEscaped = passwd.replace("@", "\\@");
            String url = "ftp://" + userText.getText() + ":" + passwdEscaped + "@" + hostText.getText() + ":" + portText.getText();
            fTPAddress.setFTPURL(url);

            fTPAddress.setFTPLogin(userText.getText());
            fTPAddress.setFTPLoginPassword(new String(passwdFld.getPassword()));

            fTPAddress.setDirListStyle(listStylesCombo.getSelectedItem() != null ? listStylesCombo.getSelectedItem().toString() : "");

            fTPAddress.setSecureFTPType(securedFTPTypeCombo.getSelectedItem() != null ? securedFTPTypeCombo.getSelectedItem().toString() : "None");

            if (fTPAddress.getSecureFTPType() == null || fTPAddress.getSecureFTPType().equals(FTPConstants.SEC_FTP_TYPES[0])) {
                fTPAddress.setKeyStore(null);
                fTPAddress.setKeyStorePassword(null);
                fTPAddress.setTrustStore(null);
                fTPAddress.setTrustStorePassword(null);
                fTPAddress.setKeyAlias(null);
                fTPAddress.setKeyPassword(null);
                fTPAddress.setEnableCCC(enableCCCCheckLab.isSelected());
            } else {
                fTPAddress.setKeyStore(keyStoreText.getText());
                fTPAddress.setKeyStorePassword(new String(keyStorePasswdText.getPassword()));
                fTPAddress.setTrustStore(trustStoreText.getText());
                fTPAddress.setTrustStorePassword(new String(trustStorePasswdFld.getPassword()));
                fTPAddress.setKeyAlias(keyAliasText.getText());
                fTPAddress.setKeyPassword(new String(keyAliasPasswdFld.getPassword()));
                fTPAddress.setEnableCCC(enableCCCCheckLab.isSelected());
            }

            fTPAddress.setUseUserDefinedHeuristics(useUDStyleCheck.isSelected());
            if (fTPAddress.getUseUserDefinedHeuristics()) {
                fTPAddress.setUserDefDirListHeuristics(userListStyleLocText.getText());
                fTPAddress.setUserDefDirListStyle(userListStyleNameText.getText());
            } else {
                fTPAddress.setUserDefDirListHeuristics("");
                fTPAddress.setUserDefDirListStyle("");
            }

            fTPAddress.setCmdChannelTimeout(cmdChannelTimeoutText.getText());
            fTPAddress.setDataChannelTimeout(dataChannelTimeoutText.getText());
            fTPAddress.setControlChannelEncoding(controlEncodingText.getText());
            fTPAddress.setPersistenceBaseDir(persistLocText.getText());

            Port port = (Port) fTPAddress.getParent();
            Binding binding = port.getBinding().get();
            String operationName = getOperationName();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();

            // only 1
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    private boolean commitBinding(FTPBinding fTPBinding) {
        WSDLModel wsdlModel = fTPBinding.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }
            FTPAddress ftpAddress = getFTPAddressPerSelectedPort();
            if (ftpAddress != null) {
                String url = "ftp://" + userText.getText() + ":" + new String(passwdFld.getPassword()) + "@" + hostText.getText() + ":" + portText.getText();
                ftpAddress.setFTPURL(url);

                ftpAddress.setFTPLogin(userText.getText());
                ftpAddress.setFTPLoginPassword(new String(passwdFld.getPassword()));

                ftpAddress.setDirListStyle(listStylesCombo.getSelectedItem() != null ? listStylesCombo.getSelectedItem().toString() : "");

                ftpAddress.setSecureFTPType(securedFTPTypeCombo.getSelectedItem() != null ? securedFTPTypeCombo.getSelectedItem().toString() : "None");

                if (ftpAddress.getSecureFTPType() == null || ftpAddress.getSecureFTPType().equals(FTPConstants.SEC_FTP_TYPES[0])) {
                    ftpAddress.setKeyStore(null);
                    ftpAddress.setKeyStorePassword(null);
                    ftpAddress.setTrustStore(null);
                    ftpAddress.setTrustStorePassword(null);
                    ftpAddress.setKeyAlias(null);
                    ftpAddress.setKeyPassword(null);
                    ftpAddress.setEnableCCC(enableCCCCheckLab.isSelected());
                } else {
                    ftpAddress.setKeyStore(keyStoreText.getText());
                    ftpAddress.setKeyStorePassword(new String(keyStorePasswdText.getPassword()));
                    ftpAddress.setTrustStore(trustStoreText.getText());
                    ftpAddress.setTrustStorePassword(new String(trustStorePasswdFld.getPassword()));
                    ftpAddress.setKeyAlias(keyAliasText.getText());
                    ftpAddress.setKeyPassword(new String(keyAliasPasswdFld.getPassword()));
                    ftpAddress.setEnableCCC(enableCCCCheckLab.isSelected());
                }

                ftpAddress.setUseUserDefinedHeuristics(useUDStyleCheck.isSelected());
                if (ftpAddress.getUseUserDefinedHeuristics()) {
                    ftpAddress.setUserDefDirListHeuristics(userListStyleLocText.getText());
                    ftpAddress.setUserDefDirListStyle(userListStyleNameText.getText());
                } else {
                    ftpAddress.setUserDefDirListHeuristics("");
                    ftpAddress.setUserDefDirListStyle("");
                }

                ftpAddress.setCmdChannelTimeout(cmdChannelTimeoutText.getText());
                ftpAddress.setDataChannelTimeout(dataChannelTimeoutText.getText());
                ftpAddress.setControlChannelEncoding(controlEncodingText.getText());
                ftpAddress.setPersistenceBaseDir(persistLocText.getText());
            }

            Binding binding = (Binding) fTPBinding.getParent();

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = getOperationName();

            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    public FTPAddress getFTPAddressPerSelectedPort() {
        FTPAddress address = null;
        Port selectedServicePort = (Port) servicePortComboBox.getSelectedItem();
        if (selectedServicePort != null) {
            Binding binding = selectedServicePort.getBinding().get();
            String selBindingName = bindingNameComboBox.getSelectedItem() != null ? bindingNameComboBox.getSelectedItem().toString() : "";
            if ((binding != null) && (binding.getName().
                    equals(selBindingName))) {
                Iterator<FTPAddress> ftpAddresses = selectedServicePort.getExtensibilityElements(FTPAddress.class).
                        iterator();
                // 1 fileaddress for 1 binding
                while (ftpAddresses.hasNext()) {
                    return ftpAddresses.next();
                }
            }
        }
        return address;
    }

    private boolean commitMessage(FTPMessage fTPMessage) {
        return true;
    }

    private boolean commitOperation(FTPOperation ftpOperation) {
        Object obj = ftpOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding =
                    (Binding) ((BindingOperation) obj).getParent();
            Collection<FTPBinding> bindings =
                    parentBinding.getExtensibilityElements(FTPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }

    private boolean commitPort(Port port) {
        Collection<FTPAddress> address = port.getExtensibilityElements(FTPAddress.class);
        if (address != null && address.size() > 0) {
            FTPAddress ftpAddress = address.iterator().next();
            return commitAddress(ftpAddress);
        }
        return false;
    }

    private boolean commitTransfer(FTPTransfer fTPTransfer) {
        return true;
    }

    private void initCustomComponents() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        javax.swing.JPanel tmpPanel = new javax.swing.JPanel();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        ftpReqRespMessageCfgPanel.add(tmpPanel, gridBagConstraints);
        /**
         * invisible components used to keep status:
         */
        servicePortComboBox = new JComboBox();
        bindingNameComboBox = new JComboBox();
        portTypeComboBox = new JComboBox();
        operationNameComboBox = new JComboBox();
        /**
         * set mnemonic for tabs
         */
        ftpConfigTabbedPane.setMnemonicAt(0, KeyEvent.VK_I);
        ftpConfigTabbedPane.setMnemonicAt(1, KeyEvent.VK_S);
        ftpConfigTabbedPane.setMnemonicAt(2, KeyEvent.VK_V);
        ftpConfigTabbedPane.addChangeListener(mTabChangeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == ftpConfigTabbedPane) {
                    validateMe(true);
                }
            }
        });
        this.addAncestorListener(this);
    }

    /**
     * Populate the view with the given the model component
     * @param qName
     * @param component
     */
    public void populateView(QName qName, WSDLComponent component) {
        cleanUp();
        mQName = qName;
        mWsdlComponent = component;
        resetView();
        populateView(mWsdlComponent);
        initListeners();
    }

    private void cleanUp() {
        mQName = null;
        mWsdlComponent = null;
    }

    private void resetView() {
        enableCCCCheckLab.removeItemListener(mItemListener);
        listStylesCombo.removeItemListener(mItemListener);
        securedFTPTypeCombo.removeItemListener(mItemListener);
        transferModeCombo.removeItemListener(mItemListener);
        useUDStyleCheck.removeItemListener(mItemListener);

        cmdChannelTimeoutText.removeActionListener(mActionListener);
        controlEncodingText.removeActionListener(mActionListener);
        dataChannelTimeoutText.removeActionListener(mActionListener);
        persistLocText.removeActionListener(mActionListener);
        hostText.removeActionListener(mActionListener);
        keyAliasPasswdFld.removeActionListener(mActionListener);
        keyAliasText.removeActionListener(mActionListener);
        keyStorePasswdText.removeActionListener(mActionListener);
        keyStoreText.removeActionListener(mActionListener);
        passwdFld.removeActionListener(mActionListener);
        portText.removeActionListener(mActionListener);
        trustStorePasswdFld.removeActionListener(mActionListener);
        trustStoreText.removeActionListener(mActionListener);
        userListStyleLocText.removeActionListener(mActionListener);
        userListStyleNameText.removeActionListener(mActionListener);
        userText.removeActionListener(mActionListener);

        cmdChannelTimeoutText.getDocument().removeDocumentListener(mDocumentListener);
        controlEncodingText.getDocument().removeDocumentListener(mDocumentListener);
        dataChannelTimeoutText.getDocument().removeDocumentListener(mDocumentListener);
        persistLocText.getDocument().removeDocumentListener(mDocumentListener);
        hostText.getDocument().removeDocumentListener(mDocumentListener);
        keyAliasPasswdFld.getDocument().removeDocumentListener(mDocumentListener);
        keyAliasText.getDocument().removeDocumentListener(mDocumentListener);
        keyStorePasswdText.getDocument().removeDocumentListener(mDocumentListener);
        keyStoreText.getDocument().removeDocumentListener(mDocumentListener);
        passwdFld.getDocument().removeDocumentListener(mDocumentListener);
        portText.getDocument().removeDocumentListener(mDocumentListener);
        trustStorePasswdFld.getDocument().removeDocumentListener(mDocumentListener);
        trustStoreText.getDocument().removeDocumentListener(mDocumentListener);
        userListStyleLocText.getDocument().removeDocumentListener(mDocumentListener);
        userListStyleNameText.getDocument().removeDocumentListener(mDocumentListener);
        userText.getDocument().removeDocumentListener(mDocumentListener);

        messageCorrelateLab.setSelected(true);
        // set to the default
        enableCCCCheckLab.setSelected(false);

        listStylesCombo.removeAllItems();
        for (int i = 0; i < FTPConstants.LIST_STYLES.length; i++) {
            listStylesCombo.addItem(FTPConstants.LIST_STYLES[i]);
        }
        securedFTPTypeCombo.removeAllItems();
        for (int i = 0; i < FTPConstants.SEC_FTP_TYPES.length; i++) {
            securedFTPTypeCombo.addItem(FTPConstants.SEC_FTP_TYPES[i]);
        }
        transferModeCombo.removeAllItems();
        for (int i = 0; i < FTPConstants.TRANSFER_MODES.length; i++) {
            transferModeCombo.addItem(FTPConstants.TRANSFER_MODES[i]);
        }

        useUDStyleCheck.setSelected(false);

        messageRepoText.setText("");
        cmdChannelTimeoutText.setText("45000");
        controlEncodingText.setText("");
        dataChannelTimeoutText.setText("45000");
        persistLocText.setText("");
        hostText.setText("localhost");
        keyAliasPasswdFld.setText("");
        keyAliasText.setText("");
        keyStorePasswdText.setText("");
        keyStoreText.setText("");
        passwdFld.setText("");
        portText.setText("");
        trustStorePasswdFld.setText("");
        trustStoreText.setText("");
        userListStyleLocText.setText("");
        userListStyleNameText.setText("");
        userText.setText("");
    }

    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof FTPAddress) {
                populateFTPAddress((FTPAddress) component);
            } else if (component instanceof FTPBinding) {
                populateFTPBinding((FTPBinding) component, null);
            } else if (component instanceof Port) {
                Collection<FTPAddress> address = ((Port) component).getExtensibilityElements(FTPAddress.class);
                if (!address.isEmpty()) {
                    populateFTPAddress(address.iterator().next());
                }
            } else if (component instanceof FTPMessage) {
                Object obj = ((FTPMessage) component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation) ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFTPBinding(bindings.iterator().next(), null);
                    }
                }
            } else if (component instanceof FTPTransfer) {
                Object obj = ((FTPTransfer) component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation) ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFTPBinding(bindings.iterator().next(), null);
                    }
                }
            } else if (component instanceof FTPOperation) {
                Object obj = ((FTPOperation) component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
                    Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFTPBinding(bindings.iterator().next(), null);
                    }
                }
            }
        }
    }

    private void initListeners() {
        if (mItemListener == null) {
            mItemListener = new MyItemListener();
        }

        if (mActionListener == null) {
            mActionListener = new MyActionListener();
        }

        if (mDocumentListener == null) {
            mDocumentListener = new MyDocumentListener();
        }


        enableCCCCheckLab.addItemListener(mItemListener);
        listStylesCombo.addItemListener(mItemListener);
        securedFTPTypeCombo.addItemListener(mItemListener);
        transferModeCombo.addItemListener(mItemListener);
        useUDStyleCheck.addItemListener(mItemListener);

        cmdChannelTimeoutText.addActionListener(mActionListener);
        controlEncodingText.addActionListener(mActionListener);
        dataChannelTimeoutText.addActionListener(mActionListener);
        persistLocText.addActionListener(mActionListener);
        hostText.addActionListener(mActionListener);
        keyAliasPasswdFld.addActionListener(mActionListener);
        keyAliasText.addActionListener(mActionListener);
        keyStorePasswdText.addActionListener(mActionListener);
        keyStoreText.addActionListener(mActionListener);
        passwdFld.addActionListener(mActionListener);
        portText.addActionListener(mActionListener);
        trustStorePasswdFld.addActionListener(mActionListener);
        trustStoreText.addActionListener(mActionListener);
        userListStyleLocText.addActionListener(mActionListener);
        userListStyleNameText.addActionListener(mActionListener);
        userText.addActionListener(mActionListener);

        cmdChannelTimeoutText.getDocument().addDocumentListener(mDocumentListener);
        controlEncodingText.getDocument().addDocumentListener(mDocumentListener);
        dataChannelTimeoutText.getDocument().addDocumentListener(mDocumentListener);
        persistLocText.getDocument().addDocumentListener(mDocumentListener);
        hostText.getDocument().addDocumentListener(mDocumentListener);
        keyAliasPasswdFld.getDocument().addDocumentListener(mDocumentListener);
        keyAliasText.getDocument().addDocumentListener(mDocumentListener);
        keyStorePasswdText.getDocument().addDocumentListener(mDocumentListener);
        keyStoreText.getDocument().addDocumentListener(mDocumentListener);
        passwdFld.getDocument().addDocumentListener(mDocumentListener);
        portText.getDocument().addDocumentListener(mDocumentListener);
        trustStorePasswdFld.getDocument().addDocumentListener(mDocumentListener);
        trustStoreText.getDocument().addDocumentListener(mDocumentListener);
        userListStyleLocText.getDocument().addDocumentListener(mDocumentListener);
        userListStyleNameText.getDocument().addDocumentListener(mDocumentListener);
        userText.getDocument().addDocumentListener(mDocumentListener);
    }

    public class MyItemListener implements ItemListener {

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            handleItemStateChanged(evt);
        }
    }

    public class MyActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            handleActionPerformed(evt);
        }
    }

    public class MyDocumentListener implements DocumentListener {
        // Handle insertions into the text field
        public void insertUpdate(DocumentEvent event) {
            if ( mProxy != null )
                ((ValidationProxy)mProxy).validatePlugin();
            else
                validateMe(true);
        }

        // Handle deletions	from the text field
        public void removeUpdate(DocumentEvent event) {
            if ( mProxy != null )
                ((ValidationProxy)mProxy).validatePlugin();
            else
                validateMe(true);
        }

        // Handle changes to the text field
        public void changedUpdate(DocumentEvent event) {
            // empty
        }
    }

    private void handleItemStateChanged(ItemEvent evt) {
        // when check box checked + combo selected etc.
        if (evt.getSource() == securedFTPTypeCombo) {
            handleSecuredFTPTypeSelection(evt);
        } else if (evt.getSource() == useUDStyleCheck) {
            handleUseUDStyleSelected(evt);
        }
    }

    private void handleSecuredFTPTypeSelection(ItemEvent evt) {
        if ( mProxy != null )
            ((ValidationProxy)mProxy).validatePlugin();
        else
            validateMe(true);
    }

    private void handleUseUDStyleSelected(ItemEvent evt) {
        if ( mProxy != null )
            ((ValidationProxy)mProxy).validatePlugin();
        else
            validateMe(true);
    }

    private void handleActionPerformed(ActionEvent evt) {
    }

    private void populateFTPAddress(FTPAddress address) {
        enableCCCCheckLab.setSelected(address.getEnableCCC());
        listStylesCombo.setSelectedItem(address.getDirListStyle() != null ? address.getDirListStyle() : "UNIX");
        securedFTPTypeCombo.setSelectedItem(address.getSecureFTPType() != null ? address.getSecureFTPType() : "None");
        transferModeCombo.setSelectedItem(address.getTransferMode() != null ? address.getTransferMode() : "BINARY");
        useUDStyleCheck.setSelected(address.getUseUserDefinedHeuristics());

        String url = address.getFTPURL();

        if (url != null) {
            FTPAddressURL ftpURL = new FTPAddressURL(url);
            Vector results = new Vector();
            boolean ret = ftpURL.parse(results, address);
            if (!ret || results.size() > 0) {
                // invalid URL, report error
            }
            hostText.setText(ftpURL.getHost());
            portText.setText(ftpURL.getPort());
            if (address.getFTPLogin() != null && address.getFTPLogin().trim().length() > 0) {
                userText.setText(address.getFTPLogin());
            } else {
                userText.setText(ftpURL.getUser() != null ? ftpURL.getUser() : "");
            }
            if (address.getFTPLoginPassword() != null && address.getFTPLoginPassword().trim().length() > 0) {
                passwdFld.setText(address.getFTPLoginPassword());
            } else {
                passwdFld.setText(ftpURL.getPassword() != null ? ftpURL.getPassword() : "");
            }
        } else {
            hostText.setText(FTPConstants.DEFAULT_HOST);
            portText.setText(FTPConstants.DEFAULT_PORT);
            userText.setText(address.getFTPLogin() != null ? address.getFTPLogin() : "");
            passwdFld.setText(address.getFTPLoginPassword() != null ? address.getFTPLoginPassword() : "");
        }

        keyAliasPasswdFld.setText(address.getKeyPassword());
        keyAliasText.setText(address.getKeyAlias());
        keyStorePasswdText.setText(address.getKeyStorePassword());
        keyStoreText.setText(address.getKeyStore());
        trustStorePasswdFld.setText(address.getTrustStorePassword());
        trustStoreText.setText(address.getTrustStore());
        userListStyleLocText.setText(address.getUserDefDirListHeuristics());
        userListStyleNameText.setText(address.getUserDefDirListStyle());
        cmdChannelTimeoutText.setText(address.getCmdChannelTimeout());
        controlEncodingText.setText(address.getControlChannelEncoding());
        dataChannelTimeoutText.setText(address.getDataChannelTimeout());
        persistLocText.setText(address.getPersistenceBaseDir());
        validateMe(true);
    }

    private void populateFTPBinding(FTPBinding fTPBinding, Object object) {
    }

    private void updateDescriptionArea(FocusEvent evt) {
        descriptionPanel.setText("");

        String[] desc = null;

        if (evt.getSource() == cmdChannelTimeoutText) {
            desc = new String[]{"FTP Command Channel Timeout\n\n",
                        mBundle.getString("DESC_Attribute_cmdChannelTimeout")
                    };
        } else if (evt.getSource() == controlEncodingText) {
            desc = new String[]{"FTP Control Channel Encoding\n\n",
                        mBundle.getString("DESC_Attribute_controlChannelEncoding")
                    };
        } else if (evt.getSource() == dataChannelTimeoutText) {
            desc = new String[]{"FTP Data Channel Timeout\n\n",
                        mBundle.getString("DESC_Attribute_dataChannelTimeout")
                    };
        } else if (evt.getSource() == enableCCCCheckLab) {
            desc = new String[]{"FTP over SSL explicit SSL enabled Clear Control Channel\n\n",
                        mBundle.getString("DESC_Attribute_enableCCC")
                    };
        } else if (evt.getSource() == hostText) {
            desc = new String[]{"FTP Host name or IP\n\n",
                        mBundle.getString("DESC_Attribute_host")
                    };
        } else if (evt.getSource() == keyAliasPasswdFld) {
            desc = new String[]{"Key Alias Password\n\n",
                        mBundle.getString("DESC_Attribute_keyPassword")
                    };
        } else if (evt.getSource() == keyAliasText) {
            desc = new String[]{"Key Alias\n\n",
                        mBundle.getString("DESC_Attribute_keyAlias")
                    };
        } else if (evt.getSource() == keyStorePasswdText) {
            desc = new String[]{"Key Store Password\n\n",
                        mBundle.getString("DESC_Attribute_keyStorePassword")
                    };
        } else if (evt.getSource() == keyStoreText) {
            desc = new String[]{"Key Store Location\n\n",
                        mBundle.getString("DESC_Attribute_keyStore")
                    };
        } else if (evt.getSource() == listStylesCombo) {
            desc = new String[]{"Directory List Style\n\n",
                        mBundle.getString("DESC_Attribute_dirListStyle")
                    };
        } else if (evt.getSource() == passwdFld) {
            desc = new String[]{"FTP Login Password\n\n",
                        mBundle.getString("DESC_Attribute_password")
                    };
        } else if (evt.getSource() == portText) {
            desc = new String[]{"FTP server port (default 21)\n\n",
                        mBundle.getString("DESC_Attribute_port")
                    };
        } else if (evt.getSource() == securedFTPTypeCombo) {
            desc = new String[]{"Secured FTP Type\n\n",
                        mBundle.getString("DESC_Attribute_securedFTP")
                    };
        } else if (evt.getSource() == transferModeCombo) {
            desc = new String[]{"FTP Transfer Mode\n\n",
                        mBundle.getString("DESC_Attribute_mode")
                    };
        } else if (evt.getSource() == trustStorePasswdFld) {
            desc = new String[]{"Trust Store Password\n\n",
                        mBundle.getString("DESC_Attribute_trustStorePassword")
                    };
        } else if (evt.getSource() == trustStoreText) {
            desc = new String[]{"Trust Store\n\n",
                        mBundle.getString("DESC_Attribute_trustStore")
                    };
        } else if (evt.getSource() == useUDStyleCheck) {
            desc = new String[]{"Use User Defined Directory List Style\n\n",
                        mBundle.getString("DESC_Attribute_useUserDefinedHeuristics")
                    };
        } else if (evt.getSource() == userListStyleLocText) {
            desc = new String[]{"User Defined Directory List Heuristics Location\n\n",
                        mBundle.getString("DESC_Attribute_userDefDirListHeuristics")
                    };
        } else if (evt.getSource() == userListStyleNameText) {
            desc = new String[]{"User Defined Directory List Style\n\n",
                        mBundle.getString("DESC_Attribute_userDefDirListStyle")
                    };
        } else if (evt.getSource() == userText) {
            desc = new String[]{"FTP Login User ID\n\n",
                        mBundle.getString("DESC_Attribute_user")
                    };
        } else if (evt.getSource() == messageCorrelateLab) {
            desc = new String[]{"Correlate Request and Response\n\n",
                        mBundle.getString("DESC_Attribute_Message_messageCorrelate")
                    };
        } else if (evt.getSource() == messageRepoText) {
            desc = new String[]{"Messaging Repository Base Dir\n\n",
                        mBundle.getString("DESC_Attribute_Message_messageRepository")
                    };
        } else if (evt.getSource() == persistLocText ) {
            desc = new String[]{"Persistence Base Location\n\n",
                        mBundle.getString("DESC_Attribute_persistenceBaseLocation")
                    };
        }

        if (desc != null) {
            descriptionPanel.setText(desc[0], desc[1]);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        ftpReqRespMessageCfgPanel = new javax.swing.JPanel();
        ftpConfigTabbedPane = new javax.swing.JTabbedPane();
        connectionPanel = new javax.swing.JPanel();
        hostLab = new javax.swing.JLabel();
        hostText = new javax.swing.JTextField();
        portLab = new javax.swing.JLabel();
        portText = new javax.swing.JTextField();
        userLab = new javax.swing.JLabel();
        userText = new javax.swing.JTextField();
        passwordLab = new javax.swing.JLabel();
        passwdFld = new javax.swing.JPasswordField();
        listStyleLab = new javax.swing.JLabel();
        listStylesCombo = new javax.swing.JComboBox();
        transferModeLab = new javax.swing.JLabel();
        transferModeCombo = new javax.swing.JComboBox();
        messageRepoLab = new javax.swing.JLabel();
        messageRepoText = new javax.swing.JTextField();
        messageCorrelateLab = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        connectionPanelTitleLab = new javax.swing.JLabel();
        hostImgLab = new javax.swing.JLabel();
        portImgLab = new javax.swing.JLabel();
        userImgLab = new javax.swing.JLabel();
        passwdImgLab = new javax.swing.JLabel();
        listStyleImgLab = new javax.swing.JLabel();
        transModeImgLab = new javax.swing.JLabel();
        repoImgLab = new javax.swing.JLabel();
        msgCorrelateImgLab = new javax.swing.JLabel();
        sslConfigPanel = new javax.swing.JPanel();
        securedFTPTypeLab = new javax.swing.JLabel();
        securedFTPTypeCombo = new javax.swing.JComboBox();
        enableCCCCheckLab = new javax.swing.JCheckBox();
        keyStoreLab = new javax.swing.JLabel();
        keyStoreText = new javax.swing.JTextField();
        keyStorePasswdLab = new javax.swing.JLabel();
        keyStorePasswdText = new javax.swing.JPasswordField();
        keyAliasLab = new javax.swing.JLabel();
        keyAliasText = new javax.swing.JTextField();
        keyAliasPasswdLab = new javax.swing.JLabel();
        trustStoreLab = new javax.swing.JLabel();
        keyAliasPasswdFld = new javax.swing.JPasswordField();
        trustStoreText = new javax.swing.JTextField();
        trustStorePasswdLab = new javax.swing.JLabel();
        trustStorePasswdFld = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();
        sslPanelTitleLab = new javax.swing.JLabel();
        secTypeImgLab = new javax.swing.JLabel();
        cccImgLab = new javax.swing.JLabel();
        ksLocImgLab = new javax.swing.JLabel();
        ksPasswdImgLab = new javax.swing.JLabel();
        kaliasImgLab = new javax.swing.JLabel();
        kaliasPasswdImgLab = new javax.swing.JLabel();
        tsLocImgLab = new javax.swing.JLabel();
        tsPasswdImgLab = new javax.swing.JLabel();
        advancedPanel = new javax.swing.JPanel();
        controlEncodingLab = new javax.swing.JLabel();
        controlEncodingText = new javax.swing.JTextField();
        cmdChannelTimeoutLab = new javax.swing.JLabel();
        cmdChannelTimeoutText = new javax.swing.JTextField();
        dataChannelTimeoutLab = new javax.swing.JLabel();
        dataChannelTimeoutText = new javax.swing.JTextField();
        useUDStyleCheck = new javax.swing.JCheckBox();
        userListStyleNameLab = new javax.swing.JLabel();
        userListStyleNameText = new javax.swing.JTextField();
        userListStyleLocLab = new javax.swing.JLabel();
        userListStyleLocText = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        advancedPanelTitleLab1 = new javax.swing.JLabel();
        ctlEncodingImgLab = new javax.swing.JLabel();
        cmdTimeoutImgLab = new javax.swing.JLabel();
        dataTimeoutImgLab = new javax.swing.JLabel();
        useUDImgLab = new javax.swing.JLabel();
        udStyleImgLab = new javax.swing.JLabel();
        udStyleCfgImgLab = new javax.swing.JLabel();
        persistLocLab1 = new javax.swing.JLabel();
        persistLocImgLab1 = new javax.swing.JLabel();
        persistLocText = new javax.swing.JTextField();
        descPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        ftpReqRespMessageCfgPanel.setMinimumSize(new java.awt.Dimension(610, 508));
        ftpReqRespMessageCfgPanel.setName("ftpReqRespMessageCfgPanel"); // NOI18N
        ftpReqRespMessageCfgPanel.setPreferredSize(new java.awt.Dimension(610, 508));
        ftpReqRespMessageCfgPanel.setLayout(new java.awt.GridBagLayout());

        ftpConfigTabbedPane.setMinimumSize(new java.awt.Dimension(610, 460));
        ftpConfigTabbedPane.setName("ftpConfigTabbedPane"); // NOI18N
        ftpConfigTabbedPane.setPreferredSize(new java.awt.Dimension(610, 460));

        connectionPanel.setMinimumSize(new java.awt.Dimension(610, 460));
        connectionPanel.setName("connectionPanel"); // NOI18N
        connectionPanel.setPreferredSize(new java.awt.Dimension(610, 460));
        connectionPanel.setLayout(new java.awt.GridBagLayout());

        hostLab.setLabelFor(hostText);
        org.openide.awt.Mnemonics.setLocalizedText(hostLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.hostLab.text")); // NOI18N
        hostLab.setName("hostLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(hostLab, gridBagConstraints);
        hostLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.hostLab.AccessibleContext.accessibleDescription")); // NOI18N

        hostText.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.hostText.text")); // NOI18N
        hostText.setMinimumSize(new java.awt.Dimension(60, 20));
        hostText.setName("hostText"); // NOI18N
        hostText.setPreferredSize(new java.awt.Dimension(60, 20));
        hostText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostTextActionPerformed(evt);
            }
        });
        hostText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                hostTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                hostTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(hostText, gridBagConstraints);
        hostText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.hostText.AccessibleContext.accessibleName")); // NOI18N
        hostText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.hostText.AccessibleContext.accessibleDescription")); // NOI18N

        portLab.setLabelFor(portText);
        org.openide.awt.Mnemonics.setLocalizedText(portLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.portLab.text")); // NOI18N
        portLab.setName("portLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(portLab, gridBagConstraints);
        portLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.portLab.AccessibleContext.accessibleDescription")); // NOI18N

        portText.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.portText.text")); // NOI18N
        portText.setName("portText"); // NOI18N
        portText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                portTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                portTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(portText, gridBagConstraints);
        portText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.portText.AccessibleContext.accessibleName")); // NOI18N
        portText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.portText.AccessibleContext.accessibleDescription")); // NOI18N

        userLab.setLabelFor(userText);
        org.openide.awt.Mnemonics.setLocalizedText(userLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userLab.text")); // NOI18N
        userLab.setName("userLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(userLab, gridBagConstraints);
        userLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userLab.AccessibleContext.accessibleDescription")); // NOI18N

        userText.setName("userText"); // NOI18N
        userText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userTextActionPerformed(evt);
            }
        });
        userText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                userTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(userText, gridBagConstraints);
        userText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userText.AccessibleContext.accessibleName")); // NOI18N
        userText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userText.AccessibleContext.accessibleDescription")); // NOI18N

        passwordLab.setLabelFor(passwdFld);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.passwordLab.text")); // NOI18N
        passwordLab.setName("passwordLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(passwordLab, gridBagConstraints);
        passwordLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.passwordLab.AccessibleContext.accessibleDescription")); // NOI18N

        passwdFld.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.passwdFld.text")); // NOI18N
        passwdFld.setName("passwdFld"); // NOI18N
        passwdFld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwdFldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                passwdFldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(passwdFld, gridBagConstraints);
        passwdFld.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.passwdFld.AccessibleContext.accessibleName")); // NOI18N
        passwdFld.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.passwdFld.AccessibleContext.accessibleDescription")); // NOI18N

        listStyleLab.setLabelFor(listStylesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(listStyleLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.listStyleLab.text")); // NOI18N
        listStyleLab.setName("listStyleLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(listStyleLab, gridBagConstraints);
        listStyleLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.listStyleLab.AccessibleContext.accessibleDescription")); // NOI18N

        listStylesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        listStylesCombo.setName("listStylesCombo"); // NOI18N
        listStylesCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                listStylesComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                listStylesComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(listStylesCombo, gridBagConstraints);
        listStylesCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.listStylesCombo.AccessibleContext.accessibleName")); // NOI18N
        listStylesCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.listStylesCombo.AccessibleContext.accessibleDescription")); // NOI18N

        transferModeLab.setLabelFor(transferModeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(transferModeLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.transferModeLab.text")); // NOI18N
        transferModeLab.setName("transferModeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(transferModeLab, gridBagConstraints);
        transferModeLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.transferModeLab.AccessibleContext.accessibleDescription")); // NOI18N

        transferModeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        transferModeCombo.setName("transferModeCombo"); // NOI18N
        transferModeCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                transferModeComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                transferModeComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(transferModeCombo, gridBagConstraints);
        transferModeCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.transferModeCombo.AccessibleContext.accessibleName")); // NOI18N
        transferModeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.transferModeCombo.AccessibleContext.accessibleDescription")); // NOI18N

        messageRepoLab.setLabelFor(messageRepoText);
        org.openide.awt.Mnemonics.setLocalizedText(messageRepoLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.messageRepoLab.text")); // NOI18N
        messageRepoLab.setName("messageRepoLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(messageRepoLab, gridBagConstraints);
        messageRepoLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.messageRepoLab.AccessibleContext.accessibleDescription")); // NOI18N

        messageRepoText.setName("messageRepoText"); // NOI18N
        messageRepoText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageRepoTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                messageRepoTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(messageRepoText, gridBagConstraints);
        messageRepoText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.messageRepoText.AccessibleContext.accessibleName")); // NOI18N
        messageRepoText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.messageRepoText.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(messageCorrelateLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.messageCorrelateLab.text")); // NOI18N
        messageCorrelateLab.setName("messageCorrelateLab"); // NOI18N
        messageCorrelateLab.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageCorrelateLabFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                messageCorrelateLabFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        connectionPanel.add(messageCorrelateLab, gridBagConstraints);
        messageCorrelateLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.messageCorrelateLab.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 150, 10, 10);
        connectionPanel.add(jSeparator2, gridBagConstraints);

        connectionPanelTitleLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(connectionPanelTitleLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.connectionPanelTitleLab.text")); // NOI18N
        connectionPanelTitleLab.setName("connectionPanelTitleLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        connectionPanel.add(connectionPanelTitleLab, gridBagConstraints);

        hostImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        hostImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.hostImgLab.text")); // NOI18N
        hostImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPBCConfigurationEditorProvider.ImgIconSrvCompEditable.tooltip")); // NOI18N
        hostImgLab.setName("hostImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(hostImgLab, gridBagConstraints);

        portImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        portImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.portImgLab.text")); // NOI18N
        portImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.portImgLab.toolTipText")); // NOI18N
        portImgLab.setName("portImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(portImgLab, gridBagConstraints);

        userImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        userImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userImgLab.text")); // NOI18N
        userImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userImgLab.toolTipText")); // NOI18N
        userImgLab.setName("userImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(userImgLab, gridBagConstraints);

        passwdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        passwdImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.passwdImgLab.text")); // NOI18N
        passwdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.passwdImgLab.toolTipText")); // NOI18N
        passwdImgLab.setName("passwdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(passwdImgLab, gridBagConstraints);

        listStyleImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        listStyleImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.listStyleImgLab.text")); // NOI18N
        listStyleImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.listStyleImgLab.toolTipText")); // NOI18N
        listStyleImgLab.setName("listStyleImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(listStyleImgLab, gridBagConstraints);

        transModeImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        transModeImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.transModeImgLab.text")); // NOI18N
        transModeImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.transModeImgLab.toolTipText")); // NOI18N
        transModeImgLab.setName("transModeImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(transModeImgLab, gridBagConstraints);

        repoImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        repoImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.repoImgLab.text")); // NOI18N
        repoImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.repoImgLab.toolTipText")); // NOI18N
        repoImgLab.setName("repoImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(repoImgLab, gridBagConstraints);

        msgCorrelateImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        msgCorrelateImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.msgCorrelateImgLab.text")); // NOI18N
        msgCorrelateImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.msgCorrelateImgLab.toolTipText")); // NOI18N
        msgCorrelateImgLab.setName("msgCorrelateImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        connectionPanel.add(msgCorrelateImgLab, gridBagConstraints);

        ftpConfigTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.connectionPanel.TabConstraints.tabTitle"), connectionPanel); // NOI18N

        sslConfigPanel.setMinimumSize(new java.awt.Dimension(610, 460));
        sslConfigPanel.setName("sslConfigPanel"); // NOI18N
        sslConfigPanel.setPreferredSize(new java.awt.Dimension(610, 460));
        sslConfigPanel.setLayout(new java.awt.GridBagLayout());

        securedFTPTypeLab.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        securedFTPTypeLab.setLabelFor(securedFTPTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(securedFTPTypeLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.securedFTPTypeLab.text")); // NOI18N
        securedFTPTypeLab.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        securedFTPTypeLab.setName("securedFTPTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(securedFTPTypeLab, gridBagConstraints);
        securedFTPTypeLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.securedFTPTypeLab.AccessibleContext.accessibleDescription")); // NOI18N

        securedFTPTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        securedFTPTypeCombo.setName("securedFTPTypeCombo"); // NOI18N
        securedFTPTypeCombo.setVerifyInputWhenFocusTarget(false);
        securedFTPTypeCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                securedFTPTypeComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                securedFTPTypeComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(securedFTPTypeCombo, gridBagConstraints);
        securedFTPTypeCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.securedFTPTypeCombo.AccessibleContext.accessibleName")); // NOI18N
        securedFTPTypeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.securedFTPTypeCombo.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(enableCCCCheckLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.enableCCCCheckLab.text")); // NOI18N
        enableCCCCheckLab.setName("enableCCCCheckLab"); // NOI18N
        enableCCCCheckLab.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                enableCCCCheckLabFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                enableCCCCheckLabFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(enableCCCCheckLab, gridBagConstraints);
        enableCCCCheckLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.enableCCCCheckLab.AccessibleContext.accessibleDescription")); // NOI18N

        keyStoreLab.setLabelFor(keyStoreText);
        org.openide.awt.Mnemonics.setLocalizedText(keyStoreLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStoreLab.text")); // NOI18N
        keyStoreLab.setName("keyStoreLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(keyStoreLab, gridBagConstraints);
        keyStoreLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStoreLab.AccessibleContext.accessibleDescription")); // NOI18N

        keyStoreText.setName("keyStoreText"); // NOI18N
        keyStoreText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyStoreTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyStoreTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(keyStoreText, gridBagConstraints);
        keyStoreText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStoreText.AccessibleContext.accessibleName")); // NOI18N
        keyStoreText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStoreText.AccessibleContext.accessibleDescription")); // NOI18N

        keyStorePasswdLab.setLabelFor(keyStorePasswdText);
        org.openide.awt.Mnemonics.setLocalizedText(keyStorePasswdLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStorePasswdLab.text")); // NOI18N
        keyStorePasswdLab.setFocusTraversalPolicyProvider(true);
        keyStorePasswdLab.setName("keyStorePasswdLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(keyStorePasswdLab, gridBagConstraints);
        keyStorePasswdLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStorePasswdLab.AccessibleContext.accessibleDescription")); // NOI18N

        keyStorePasswdText.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStorePasswdText.text")); // NOI18N
        keyStorePasswdText.setName("keyStorePasswdText"); // NOI18N
        keyStorePasswdText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyStorePasswdTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyStorePasswdTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(keyStorePasswdText, gridBagConstraints);
        keyStorePasswdText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStorePasswdText.AccessibleContext.accessibleName")); // NOI18N
        keyStorePasswdText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyStorePasswdText.AccessibleContext.accessibleDescription")); // NOI18N

        keyAliasLab.setLabelFor(keyAliasText);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasLab.text")); // NOI18N
        keyAliasLab.setName("keyAliasLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(keyAliasLab, gridBagConstraints);
        keyAliasLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasLab.AccessibleContext.accessibleDescription")); // NOI18N

        keyAliasText.setName("keyAliasText"); // NOI18N
        keyAliasText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyAliasTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyAliasTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(keyAliasText, gridBagConstraints);
        keyAliasText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasText.AccessibleContext.accessibleName")); // NOI18N
        keyAliasText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasText.AccessibleContext.accessibleDescription")); // NOI18N

        keyAliasPasswdLab.setLabelFor(keyAliasPasswdFld);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasPasswdLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasPasswdLab.text")); // NOI18N
        keyAliasPasswdLab.setName("keyAliasPasswdLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(keyAliasPasswdLab, gridBagConstraints);
        keyAliasPasswdLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasPasswdLab.AccessibleContext.accessibleDescription")); // NOI18N

        trustStoreLab.setLabelFor(trustStoreText);
        org.openide.awt.Mnemonics.setLocalizedText(trustStoreLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStoreLab.text")); // NOI18N
        trustStoreLab.setName("trustStoreLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(trustStoreLab, gridBagConstraints);
        trustStoreLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStoreLab.AccessibleContext.accessibleDescription")); // NOI18N

        keyAliasPasswdFld.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasPasswdFld.text")); // NOI18N
        keyAliasPasswdFld.setName("keyAliasPasswdFld"); // NOI18N
        keyAliasPasswdFld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                keyAliasPasswdFldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyAliasPasswdFldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(keyAliasPasswdFld, gridBagConstraints);
        keyAliasPasswdFld.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasPasswdFld.AccessibleContext.accessibleName")); // NOI18N
        keyAliasPasswdFld.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.keyAliasPasswdFld.AccessibleContext.accessibleDescription")); // NOI18N

        trustStoreText.setName("trustStoreText"); // NOI18N
        trustStoreText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                trustStoreTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                trustStoreTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(trustStoreText, gridBagConstraints);
        trustStoreText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStoreText.AccessibleContext.accessibleName")); // NOI18N
        trustStoreText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStoreText.AccessibleContext.accessibleDescription")); // NOI18N

        trustStorePasswdLab.setLabelFor(trustStorePasswdFld);
        org.openide.awt.Mnemonics.setLocalizedText(trustStorePasswdLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStorePasswdLab.text")); // NOI18N
        trustStorePasswdLab.setName("trustStorePasswdLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        sslConfigPanel.add(trustStorePasswdLab, gridBagConstraints);
        trustStorePasswdLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStorePasswdLab.AccessibleContext.accessibleDescription")); // NOI18N

        trustStorePasswdFld.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStorePasswdFld.text")); // NOI18N
        trustStorePasswdFld.setName("trustStorePasswdFld"); // NOI18N
        trustStorePasswdFld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                trustStorePasswdFldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                trustStorePasswdFldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(trustStorePasswdFld, gridBagConstraints);
        trustStorePasswdFld.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStorePasswdFld.AccessibleContext.accessibleName")); // NOI18N
        trustStorePasswdFld.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.trustStorePasswdFld.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 80, 10, 10);
        sslConfigPanel.add(jSeparator1, gridBagConstraints);

        sslPanelTitleLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(sslPanelTitleLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.sslPanelTitleLab.text")); // NOI18N
        sslPanelTitleLab.setName("sslPanelTitleLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sslConfigPanel.add(sslPanelTitleLab, gridBagConstraints);

        secTypeImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        secTypeImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.secTypeImgLab.text")); // NOI18N
        secTypeImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.secTypeImgLab.toolTipText")); // NOI18N
        secTypeImgLab.setName("secTypeImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(secTypeImgLab, gridBagConstraints);

        cccImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        cccImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cccImgLab.text")); // NOI18N
        cccImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cccImgLab.toolTipText")); // NOI18N
        cccImgLab.setName("cccImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(cccImgLab, gridBagConstraints);

        ksLocImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        ksLocImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ksLocImgLab.text")); // NOI18N
        ksLocImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ksLocImgLab.toolTipText")); // NOI18N
        ksLocImgLab.setName("ksLocImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(ksLocImgLab, gridBagConstraints);

        ksPasswdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        ksPasswdImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ksPasswdImgLab.text")); // NOI18N
        ksPasswdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ksPasswdImgLab.toolTipText")); // NOI18N
        ksPasswdImgLab.setName("ksPasswdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(ksPasswdImgLab, gridBagConstraints);

        kaliasImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        kaliasImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.kaliasImgLab.text")); // NOI18N
        kaliasImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.kaliasImgLab.toolTipText")); // NOI18N
        kaliasImgLab.setName("kaliasImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(kaliasImgLab, gridBagConstraints);

        kaliasPasswdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        kaliasPasswdImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.kaliasPasswdImgLab.text")); // NOI18N
        kaliasPasswdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.kaliasPasswdImgLab.toolTipText")); // NOI18N
        kaliasPasswdImgLab.setName("kaliasPasswdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(kaliasPasswdImgLab, gridBagConstraints);

        tsLocImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        tsLocImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.tsLocImgLab.text")); // NOI18N
        tsLocImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.tsLocImgLab.toolTipText")); // NOI18N
        tsLocImgLab.setName("tsLocImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(tsLocImgLab, gridBagConstraints);

        tsPasswdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        tsPasswdImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.tsPasswdImgLab.text")); // NOI18N
        tsPasswdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.tsPasswdImgLab.toolTipText")); // NOI18N
        tsPasswdImgLab.setName("tsPasswdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        sslConfigPanel.add(tsPasswdImgLab, gridBagConstraints);

        ftpConfigTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.sslConfigPanel.TabConstraints.tabTitle"), sslConfigPanel); // NOI18N

        advancedPanel.setMinimumSize(new java.awt.Dimension(610, 460));
        advancedPanel.setName("advancedPanel"); // NOI18N
        advancedPanel.setPreferredSize(new java.awt.Dimension(610, 460));
        advancedPanel.setLayout(new java.awt.GridBagLayout());

        controlEncodingLab.setLabelFor(controlEncodingText);
        org.openide.awt.Mnemonics.setLocalizedText(controlEncodingLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.controlEncodingLab.text")); // NOI18N
        controlEncodingLab.setName("controlEncodingLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        advancedPanel.add(controlEncodingLab, gridBagConstraints);
        controlEncodingLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.controlEncodingLab.AccessibleContext.accessibleDescription")); // NOI18N

        controlEncodingText.setName("controlEncodingText"); // NOI18N
        controlEncodingText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                controlEncodingTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                controlEncodingTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(controlEncodingText, gridBagConstraints);
        controlEncodingText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.controlEncodingText.AccessibleContext.accessibleName")); // NOI18N
        controlEncodingText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.controlEncodingText.AccessibleContext.accessibleDescription")); // NOI18N

        cmdChannelTimeoutLab.setLabelFor(cmdChannelTimeoutText);
        org.openide.awt.Mnemonics.setLocalizedText(cmdChannelTimeoutLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cmdChannelTimeoutLab.text")); // NOI18N
        cmdChannelTimeoutLab.setName("cmdChannelTimeoutLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        advancedPanel.add(cmdChannelTimeoutLab, gridBagConstraints);
        cmdChannelTimeoutLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cmdChannelTimeoutLab.AccessibleContext.accessibleDescription")); // NOI18N

        cmdChannelTimeoutText.setName("cmdChannelTimeoutText"); // NOI18N
        cmdChannelTimeoutText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cmdChannelTimeoutTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                cmdChannelTimeoutTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(cmdChannelTimeoutText, gridBagConstraints);
        cmdChannelTimeoutText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cmdChannelTimeoutText.AccessibleContext.accessibleName")); // NOI18N
        cmdChannelTimeoutText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cmdChannelTimeoutText.AccessibleContext.accessibleDescription")); // NOI18N

        dataChannelTimeoutLab.setLabelFor(dataChannelTimeoutText);
        org.openide.awt.Mnemonics.setLocalizedText(dataChannelTimeoutLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.dataChannelTimeoutLab.text")); // NOI18N
        dataChannelTimeoutLab.setName("dataChannelTimeoutLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        advancedPanel.add(dataChannelTimeoutLab, gridBagConstraints);
        dataChannelTimeoutLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.dataChannelTimeoutLab.AccessibleContext.accessibleDescription")); // NOI18N

        dataChannelTimeoutText.setName("dataChannelTimeoutText"); // NOI18N
        dataChannelTimeoutText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dataChannelTimeoutTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                dataChannelTimeoutTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(dataChannelTimeoutText, gridBagConstraints);
        dataChannelTimeoutText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.dataChannelTimeoutText.AccessibleContext.accessibleName")); // NOI18N
        dataChannelTimeoutText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.dataChannelTimeoutText.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useUDStyleCheck, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.useUDStyleCheck.text")); // NOI18N
        useUDStyleCheck.setName("useUDStyleCheck"); // NOI18N
        useUDStyleCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                useUDStyleCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                useUDStyleCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        advancedPanel.add(useUDStyleCheck, gridBagConstraints);
        useUDStyleCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.useUDStyleCheck.AccessibleContext.accessibleDescription")); // NOI18N

        userListStyleNameLab.setLabelFor(userListStyleNameText);
        org.openide.awt.Mnemonics.setLocalizedText(userListStyleNameLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userListStyleNameLab.text")); // NOI18N
        userListStyleNameLab.setName("userListStyleNameLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        advancedPanel.add(userListStyleNameLab, gridBagConstraints);
        userListStyleNameLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userListStyleNameLab.AccessibleContext.accessibleDescription")); // NOI18N

        userListStyleNameText.setName("userListStyleNameText"); // NOI18N
        userListStyleNameText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userListStyleNameTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                userListStyleNameTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(userListStyleNameText, gridBagConstraints);
        userListStyleNameText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userListStyleNameText.AccessibleContext.accessibleName")); // NOI18N
        userListStyleNameText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userListStyleNameText.AccessibleContext.accessibleDescription")); // NOI18N

        userListStyleLocLab.setLabelFor(userListStyleLocText);
        org.openide.awt.Mnemonics.setLocalizedText(userListStyleLocLab, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userListStyleLocLab.text")); // NOI18N
        userListStyleLocLab.setName("userListStyleLocLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        advancedPanel.add(userListStyleLocLab, gridBagConstraints);
        userListStyleLocLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.userListStyleLocLab.AccessibleContext.accessibleDescription")); // NOI18N

        userListStyleLocText.setName("userListStyleLocText"); // NOI18N
        userListStyleLocText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userListStyleLocTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                userListStyleLocTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(userListStyleLocText, gridBagConstraints);

        jSeparator3.setName("jSeparator3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 120, 10, 10);
        advancedPanel.add(jSeparator3, gridBagConstraints);

        advancedPanelTitleLab1.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(advancedPanelTitleLab1, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.advancedPanelTitleLab1.text")); // NOI18N
        advancedPanelTitleLab1.setName("advancedPanelTitleLab1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(advancedPanelTitleLab1, gridBagConstraints);

        ctlEncodingImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        ctlEncodingImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ctlEncodingImgLab.text")); // NOI18N
        ctlEncodingImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ctlEncodingImgLab.toolTipText")); // NOI18N
        ctlEncodingImgLab.setName("ctlEncodingImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        advancedPanel.add(ctlEncodingImgLab, gridBagConstraints);

        cmdTimeoutImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        cmdTimeoutImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cmdTimeoutImgLab.text")); // NOI18N
        cmdTimeoutImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.cmdTimeoutImgLab.toolTipText")); // NOI18N
        cmdTimeoutImgLab.setName("cmdTimeoutImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        advancedPanel.add(cmdTimeoutImgLab, gridBagConstraints);

        dataTimeoutImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        dataTimeoutImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.dataTimeoutImgLab.text")); // NOI18N
        dataTimeoutImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.dataTimeoutImgLab.toolTipText")); // NOI18N
        dataTimeoutImgLab.setName("dataTimeoutImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        advancedPanel.add(dataTimeoutImgLab, gridBagConstraints);

        useUDImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        useUDImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.useUDImgLab.text")); // NOI18N
        useUDImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.useUDImgLab.toolTipText")); // NOI18N
        useUDImgLab.setName("useUDImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        advancedPanel.add(useUDImgLab, gridBagConstraints);

        udStyleImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        udStyleImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.udStyleImgLab.text")); // NOI18N
        udStyleImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.udStyleImgLab.toolTipText")); // NOI18N
        udStyleImgLab.setName("udStyleImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        advancedPanel.add(udStyleImgLab, gridBagConstraints);

        udStyleCfgImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        udStyleCfgImgLab.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.udStyleCfgImgLab.text")); // NOI18N
        udStyleCfgImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.udStyleCfgImgLab.toolTipText")); // NOI18N
        udStyleCfgImgLab.setName("udStyleCfgImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        advancedPanel.add(udStyleCfgImgLab, gridBagConstraints);

        persistLocLab1.setLabelFor(persistLocText);
        org.openide.awt.Mnemonics.setLocalizedText(persistLocLab1, org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.persistLocLab1.text")); // NOI18N
        persistLocLab1.setName("persistLocLab1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        advancedPanel.add(persistLocLab1, gridBagConstraints);
        persistLocLab1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.persistLocLab1.AccessibleContext.accessibleDescription")); // NOI18N

        persistLocImgLab1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        persistLocImgLab1.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.persistLocImgLab1.text")); // NOI18N
        persistLocImgLab1.setToolTipText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.persistLocImgLab1.toolTipText")); // NOI18N
        persistLocImgLab1.setName("persistLocImgLab1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(persistLocImgLab1, gridBagConstraints);

        persistLocText.setText(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.persistLocText.text")); // NOI18N
        persistLocText.setName("persistLocText"); // NOI18N
        persistLocText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                persistLocTextActionPerformed(evt);
            }
        });
        persistLocText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                persistLocTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                persistLocTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        advancedPanel.add(persistLocText, gridBagConstraints);

        ftpConfigTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.advancedPanel.TabConstraints.tabTitle"), advancedPanel); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        ftpReqRespMessageCfgPanel.add(ftpConfigTabbedPane, gridBagConstraints);
        ftpConfigTabbedPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ftpConfigTabbedPane.AccessibleContext.accessibleName")); // NOI18N
        ftpConfigTabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ftpConfigTabbedPane.AccessibleContext.accessibleDescription")); // NOI18N

        jSplitPane1.setLeftComponent(ftpReqRespMessageCfgPanel);
        ftpReqRespMessageCfgPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ftpReqRespMessageCfgPanel.AccessibleContext.accessibleName")); // NOI18N
        ftpReqRespMessageCfgPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.ftpReqRespMessageCfgPanel.AccessibleContext.accessibleDescription")); // NOI18N

        descPanel.setMinimumSize(new java.awt.Dimension(610, 40));
        descPanel.setName("descPanel"); // NOI18N
        descPanel.setPreferredSize(new java.awt.Dimension(610, 40));
        descPanel.setLayout(new java.awt.BorderLayout());
        descriptionPanel = new DescriptionPanel();
        descPanel.add(descriptionPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setRightComponent(descPanel);
        descPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.descPanel.AccessibleContext.accessibleName")); // NOI18N
        descPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.descPanel.AccessibleContext.accessibleDescription")); // NOI18N

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jSplitPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.jSplitPane1.AccessibleContext.accessibleName")); // NOI18N
        jSplitPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FTPSettingsRequestResponseMessagePanel.class, "FTPSettingsRequestResponseMessagePanel.jSplitPane1.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void hostTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostTextActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_hostTextActionPerformed

private void userTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userTextActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_userTextActionPerformed

private void hostTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hostTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_hostTextFocusGained

private void hostTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hostTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_hostTextFocusLost

private void portTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_portTextFocusGained

private void portTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portTextFocusLost
}//GEN-LAST:event_portTextFocusLost

private void userTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_userTextFocusGained

private void userTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_userTextFocusLost

private void passwdFldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwdFldFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_passwdFldFocusGained

private void passwdFldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwdFldFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_passwdFldFocusLost

private void listStylesComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_listStylesComboFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_listStylesComboFocusGained

private void listStylesComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_listStylesComboFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_listStylesComboFocusLost

private void transferModeComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_transferModeComboFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_transferModeComboFocusGained

private void transferModeComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_transferModeComboFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_transferModeComboFocusLost

private void messageRepoTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageRepoTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_messageRepoTextFocusGained

private void messageRepoTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageRepoTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_messageRepoTextFocusLost

private void messageCorrelateLabFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageCorrelateLabFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_messageCorrelateLabFocusGained

private void messageCorrelateLabFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageCorrelateLabFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_messageCorrelateLabFocusLost

private void securedFTPTypeComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_securedFTPTypeComboFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_securedFTPTypeComboFocusGained

private void securedFTPTypeComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_securedFTPTypeComboFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_securedFTPTypeComboFocusLost

private void enableCCCCheckLabFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_enableCCCCheckLabFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_enableCCCCheckLabFocusGained

private void enableCCCCheckLabFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_enableCCCCheckLabFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_enableCCCCheckLabFocusLost

private void keyStoreTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyStoreTextFocusGained

private void keyStoreTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStoreTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_keyStoreTextFocusLost

private void keyStorePasswdTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStorePasswdTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyStorePasswdTextFocusGained

private void keyStorePasswdTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyStorePasswdTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_keyStorePasswdTextFocusLost

private void keyAliasTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyAliasTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyAliasTextFocusGained

private void keyAliasTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyAliasTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_keyAliasTextFocusLost

private void keyAliasPasswdFldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyAliasPasswdFldFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_keyAliasPasswdFldFocusGained

private void keyAliasPasswdFldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyAliasPasswdFldFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_keyAliasPasswdFldFocusLost

private void trustStoreTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStoreTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_trustStoreTextFocusGained

private void trustStoreTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStoreTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_trustStoreTextFocusLost

private void trustStorePasswdFldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStorePasswdFldFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_trustStorePasswdFldFocusGained

private void trustStorePasswdFldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_trustStorePasswdFldFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_trustStorePasswdFldFocusLost

private void controlEncodingTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_controlEncodingTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_controlEncodingTextFocusGained

private void controlEncodingTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_controlEncodingTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_controlEncodingTextFocusLost

private void cmdChannelTimeoutTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cmdChannelTimeoutTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_cmdChannelTimeoutTextFocusGained

private void cmdChannelTimeoutTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cmdChannelTimeoutTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_cmdChannelTimeoutTextFocusLost

private void dataChannelTimeoutTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataChannelTimeoutTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_dataChannelTimeoutTextFocusGained

private void dataChannelTimeoutTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataChannelTimeoutTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_dataChannelTimeoutTextFocusLost

private void useUDStyleCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_useUDStyleCheckFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_useUDStyleCheckFocusGained

private void useUDStyleCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_useUDStyleCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_useUDStyleCheckFocusLost

private void userListStyleNameTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userListStyleNameTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_userListStyleNameTextFocusGained

private void userListStyleNameTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userListStyleNameTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_userListStyleNameTextFocusLost

private void userListStyleLocTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userListStyleLocTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_userListStyleLocTextFocusGained

private void userListStyleLocTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userListStyleLocTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_userListStyleLocTextFocusLost

private void persistLocTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_persistLocTextActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_persistLocTextActionPerformed

private void persistLocTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_persistLocTextFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);//GEN-LAST:event_persistLocTextFocusGained
}

private void persistLocTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_persistLocTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_persistLocTextFocusLost

    /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        boolean result = true;
        ErrorDescription error = validateMe();
        if (error != null && ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT.equals(error.getErrorMode())) {
            result = false;
        } else if (mWsdlComponent instanceof FTPAddress) {
            result = commitAddress((FTPAddress) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPBinding) {
            result = commitBinding((FTPBinding) mWsdlComponent);
        } else if (mWsdlComponent instanceof Port) {
            result = commitPort((Port) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPMessage) {
            result = commitMessage((FTPMessage) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPTransfer) {
            result = commitTransfer((FTPTransfer) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPOperation) {
            result = commitOperation((FTPOperation) mWsdlComponent);
        } else {
            result = false;
        }
        return result;
    }

    public ErrorDescription validateMe() {
        return validateMe(false);
    }

    public ErrorDescription validateMe(boolean fireEvt) {
        ErrorDescription error = null;

        // host not null
        // port default to 21
        // user can be blank - anonymous
        // password can be blank - no password for anonymous

        // if use UD style = true
        // ud style must be specified
        // ud style cfg must be specified

        // if secure ftp is explicit SSL/implicit SSL
        // key store and trust store can not both be blank
        // 
        // enableCCC only applies to explicit SSL
        if (hostText.getText() == null || hostText.getText().trim().length() == 0) {
            error = Utilities.setError(error, "FTPSettingsValidation.MISSING_FTP_HOST");
        } else if (portText.getText() == null || portText.getText().trim().length() == 0) {
            error = Utilities.setError(error, "FTPSettingsValidation.MISSING_FTP_PORT");
        } else {
            Exception err = null;
            int port = -1;
            try {
                port = Integer.parseInt(portText.getText().trim());
            } catch (Exception e) {
                err = e;
            }
            if (err != null) {
                error = Utilities.setError(error, "FTPSettingsValidation.MISSING_MALFORMED_FTP_PORT", new Object[]{portText.getText()});
            } else if (port <= 0) {
                error = Utilities.setError(error, "FTPSettingsValidation.INVALID_FTP_PORT", new Object[]{portText.getText()});
            } else {
                String secFTPType = "None";
                if (securedFTPTypeCombo.getSelectedItem() != null) {
                    secFTPType = securedFTPTypeCombo.getSelectedItem().toString();
                }
                if (!secFTPType.equals("None") && (keyStoreText.getText() == null || keyStoreText.getText().trim().length() == 0) && (trustStoreText.getText() == null || trustStoreText.getText().trim().length() == 0)) {
                    // SSL selected but both trust store and key store are not specified
                    error = Utilities.setError(error, "FTPSettingsValidation.MISSING_KEYSTORE_OR_TRUSTSTORE_FOR_SSL", new Object[]{secFTPType});
                } else if (cmdChannelTimeoutText.getText() != null && cmdChannelTimeoutText.getText().trim().length() > 0 && Utilities.getInteger(cmdChannelTimeoutText.getText().trim()) <= 0) {
                    error = Utilities.setError(error, "FTPSettingsValidation.INVALID_CMD_CH_TM_OUT", new Object[]{cmdChannelTimeoutText.getText()});
                } else if (dataChannelTimeoutText.getText() != null && dataChannelTimeoutText.getText().trim().length() > 0 && Utilities.getInteger(dataChannelTimeoutText.getText().trim()) <= 0) {
                    error = Utilities.setError(error, "FTPSettingsValidation.INVALID_DAT_CH_TM_OUT", new Object[]{dataChannelTimeoutText.getText()});
                } else if (useUDStyleCheck.isSelected() && (userListStyleLocText.getText() == null || userListStyleLocText.getText().trim().length() == 0) && (userListStyleNameText.getText() == null || userListStyleNameText.getText().trim().length() == 0)) {
                    error = Utilities.setError(error, "FTPSettingsValidation.MISSING_UD_STYLE_INFO", new Object[]{userListStyleLocText.getText(), userListStyleNameText.getText()});
                }
            }

        }

        if (fireEvt) {
            if (error != null && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
                doFirePropertyChange(error.getErrorMode(), null, error.getErrorMessage());
            } else {
                doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT,
                        null, "");
            }
        }

        return error;
    }

    /**
     * Route the property change event to this panel
     */
    public void doFirePropertyChange(String name, Object oldValue, Object newValue) {
        if (mProxy != null) {
            mProxy.doFirePropertyChange(name, oldValue, newValue);
        } else {
            firePropertyChange(name, oldValue,
                    newValue);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedPanel;
    private javax.swing.JLabel advancedPanelTitleLab1;
    private javax.swing.JLabel cccImgLab;
    private javax.swing.JLabel cmdChannelTimeoutLab;
    private javax.swing.JTextField cmdChannelTimeoutText;
    private javax.swing.JLabel cmdTimeoutImgLab;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JLabel connectionPanelTitleLab;
    private javax.swing.JLabel controlEncodingLab;
    private javax.swing.JTextField controlEncodingText;
    private javax.swing.JLabel ctlEncodingImgLab;
    private javax.swing.JLabel dataChannelTimeoutLab;
    private javax.swing.JTextField dataChannelTimeoutText;
    private javax.swing.JLabel dataTimeoutImgLab;
    private javax.swing.JPanel descPanel;
    private javax.swing.JCheckBox enableCCCCheckLab;
    private javax.swing.JTabbedPane ftpConfigTabbedPane;
    private javax.swing.JPanel ftpReqRespMessageCfgPanel;
    private javax.swing.JLabel hostImgLab;
    private javax.swing.JLabel hostLab;
    private javax.swing.JTextField hostText;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel kaliasImgLab;
    private javax.swing.JLabel kaliasPasswdImgLab;
    private javax.swing.JLabel keyAliasLab;
    private javax.swing.JPasswordField keyAliasPasswdFld;
    private javax.swing.JLabel keyAliasPasswdLab;
    private javax.swing.JTextField keyAliasText;
    private javax.swing.JLabel keyStoreLab;
    private javax.swing.JLabel keyStorePasswdLab;
    private javax.swing.JPasswordField keyStorePasswdText;
    private javax.swing.JTextField keyStoreText;
    private javax.swing.JLabel ksLocImgLab;
    private javax.swing.JLabel ksPasswdImgLab;
    private javax.swing.JLabel listStyleImgLab;
    private javax.swing.JLabel listStyleLab;
    private javax.swing.JComboBox listStylesCombo;
    private javax.swing.JCheckBox messageCorrelateLab;
    private javax.swing.JLabel messageRepoLab;
    private javax.swing.JTextField messageRepoText;
    private javax.swing.JLabel msgCorrelateImgLab;
    private javax.swing.JPasswordField passwdFld;
    private javax.swing.JLabel passwdImgLab;
    private javax.swing.JLabel passwordLab;
    private javax.swing.JLabel persistLocImgLab1;
    private javax.swing.JLabel persistLocLab1;
    private javax.swing.JTextField persistLocText;
    private javax.swing.JLabel portImgLab;
    private javax.swing.JLabel portLab;
    private javax.swing.JTextField portText;
    private javax.swing.JLabel repoImgLab;
    private javax.swing.JLabel secTypeImgLab;
    private javax.swing.JComboBox securedFTPTypeCombo;
    private javax.swing.JLabel securedFTPTypeLab;
    private javax.swing.JPanel sslConfigPanel;
    private javax.swing.JLabel sslPanelTitleLab;
    private javax.swing.JLabel transModeImgLab;
    private javax.swing.JComboBox transferModeCombo;
    private javax.swing.JLabel transferModeLab;
    private javax.swing.JLabel trustStoreLab;
    private javax.swing.JPasswordField trustStorePasswdFld;
    private javax.swing.JLabel trustStorePasswdLab;
    private javax.swing.JTextField trustStoreText;
    private javax.swing.JLabel tsLocImgLab;
    private javax.swing.JLabel tsPasswdImgLab;
    private javax.swing.JLabel udStyleCfgImgLab;
    private javax.swing.JLabel udStyleImgLab;
    private javax.swing.JLabel useUDImgLab;
    private javax.swing.JCheckBox useUDStyleCheck;
    private javax.swing.JLabel userImgLab;
    private javax.swing.JLabel userLab;
    private javax.swing.JLabel userListStyleLocLab;
    private javax.swing.JTextField userListStyleLocText;
    private javax.swing.JLabel userListStyleNameLab;
    private javax.swing.JTextField userListStyleNameText;
    private javax.swing.JTextField userText;
    // End of variables declaration//GEN-END:variables
    public Map exportProperties() {
        Map p = null;
        if (messageRepoText != null && messageRepoText.getText() != null && messageRepoText.getText().trim().length() > 0) {
            p = new HashMap<String, Object>();
            p.put(FTPConstants.WSDL_PROP_MSGREPO, messageRepoText.getText().trim());
        }
        if (messageCorrelateLab != null) {
            if (p == null) {
                p = new HashMap<String, Object>();
            }
            p.put(FTPConstants.WSDL_PROP_REQRESPCORRELATE, new Boolean(messageCorrelateLab.isSelected()));
        }
        return p;
    }

    public void importProperties(Map p) {
        // this does not import any
    }

    public void enablePayloadProcessing(boolean enable) {
    }

    public void ancestorAdded(AncestorEvent event) {
        if (event.getSource() == this) {
            if ( mProxy != null ) {
                // embedded in CASA plugin
                // need to validate all the tabbed panels
                ((ValidationProxy)mProxy).validatePlugin();
            }
            else {
                validateMe(true);
            }
        }
    }

    public void ancestorRemoved(AncestorEvent event) {
        // not interested
    }

    public void ancestorMoved(AncestorEvent event) {
        // not interested
    }

    public boolean isValidConfiguration() {
        boolean result = true;
        ErrorDescription desc = validateMe();
        if (desc != null && desc.getErrorMode() != null && desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            result = false;
        }
        return result;
    }
}
