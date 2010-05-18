/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javacard.ri.platform.installer;

import java.awt.Component;
import org.netbeans.modules.javacard.common.KeysAndValues;
import org.netbeans.modules.javacard.common.GuiUtils;
import static org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames.*;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.Problems;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.text.Document;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.conversion.Converter;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationUI;
import org.openide.awt.HtmlRenderer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class DevicePropertiesPanel extends JPanel implements DocumentListener, FocusListener, ValidationUI, Runnable {

    private final ChangeSupport supp = new ChangeSupport(this);
    private boolean updating;
    private ValidationGroup group = ValidationGroup.create(this);
    private final ComboEditor ceditor = new ComboEditor();
    @SuppressWarnings("unchecked") //NOI18N
    public void run() {
        initComponents();
        ProtocolRenderer r = new ProtocolRenderer();
        contactedProtocolComboBox.setRenderer(r);
        loggerLevelComboBox.setRenderer(r);
        GuiUtils.filterNonNumericKeys(httpPortTextField);
        GuiUtils.filterNonNumericKeys(contactlessPortTextField);
        GuiUtils.filterNonNumericKeys(contactedPortTextField);
        GuiUtils.filterNonNumericKeys(proxy2idePortTextField);
        GuiUtils.filterNonNumericKeys(proxy2cjcrePortTextField);

        ramSpinner.setValue("1M"); //NOI18N
        e2pSpinner.setValue("8M"); //NOI18N
        corSpinner.setValue("4K"); //NOI18N
        loggerLevelComboBox.setSelectedItem(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("info"));
        contactedProtocolComboBox.setSelectedItem("T=1"); //NOI18N
        Set<Integer> ports = allPortsInUse();

        // set some default display name
        for (int i = 8019; i < 65536; i += 2) {
            if (!ports.contains(i)) {
                httpPortTextField.setText(i + ""); //NOI18N
                break;
            }
        }

        int idePort = 0;
        for (int i = 7019; i < 65536; i += 2) {
            String str = "" + i; //NOI18N
            idePort = (short) (i + 1);
            if (!ports.contains(i)) {
                proxy2cjcrePortTextField.setText(str);
                break;
            }
        }
        // always try ideport = cjcreport + 1
        for (int i = idePort; i < 65536; i += 2) {
            if (!ports.contains(i)) {
                proxy2idePortTextField.setText(i + ""); //NOI18N
                break;
            }
        }
        int cl = 8081;
        for (int i = 9025; i < 65536; i += 2) {
            if (!ports.contains(i)) {
                contactedPortTextField.setText(i + ""); //NOI18N
                break;
            }
        }
        // always try contactless = contacted + 1
        for (int i = cl; i < 65536; i += 2) {
            if (!ports.contains(i)) {
                contactlessPortTextField.setText("" + i); //NOI18N
                break;
            }
        }
        initHostModel();
        Validator<Document> portvalidator = Converter.find(String.class, 
                Document.class).convert(
                Validators.REQUIRE_NON_EMPTY_STRING.trim(),
                Validators.REQUIRE_VALID_INTEGER.trim(),
                Validators.REQUIRE_NON_NEGATIVE_NUMBER.trim(),
                Validators.trimString(
                Validators.numberRange(1, 65535)),
                new PortOverlapValidator());

        int httpPort = Integer.parseInt(httpPortTextField.getText());
        serverUrlField.setText ("http://localhost:" + httpPort); //NOI18N
        cardManagerUrlField.setText ("http://localhost:" + httpPort + "/cardmanager"); //NOI18N
        group.add (httpPortTextField, portvalidator);
        group.add (contactlessPortTextField, portvalidator);
        group.add (contactedPortTextField, portvalidator);
        group.add (proxy2cjcrePortTextField, portvalidator);
        group.add (proxy2idePortTextField, portvalidator);
        final RemoteVsLocalhostMismatchValidator remoteValidator = new RemoteVsLocalhostMismatchValidator();
        group.add (cardManagerUrlField, Validators.REQUIRE_NON_EMPTY_STRING.forString(false),
                Validators.MAY_NOT_START_WITH_DIGIT.forString(false), Validators.URL_MUST_BE_VALID.forString(false),
                remoteValidator);
        group.add (serverUrlField, Validators.REQUIRE_NON_EMPTY_STRING.forString(false),
                Validators.MAY_NOT_START_WITH_DIGIT.forString(false), Validators.URL_MUST_BE_VALID.forString(false),
                remoteValidator);
        Validator<Document> v = Converter.find(String.class, Document.class).convert(new PortMismatchValidator());
        group.add (new AbstractButton[] { remoteCheckbox }, new Validator<ButtonModel[]>() {
            public boolean validate(Problems prblms, String string, ButtonModel[] t) {
                return remoteValidator.validate(prblms, string, t[0].isSelected() + "");
            }
        });
        group.add (httpPortTextField, v);
        group.add (serverUrlField, v);
        group.add (httpPortTextField, v);
        group.add (hostComboBox, Validators.REQUIRE_NON_EMPTY_STRING,
                Validators.HOST_NAME_OR_IP_ADDRESS.forString(true), remoteValidator);
        hostComboBox.setEditor(ceditor);
        group.add (ceditor.field, Validators.REQUIRE_NON_EMPTY_STRING, Validators.HOST_NAME_OR_IP_ADDRESS);
        for (Component c : getComponents()) {
            localizeName (c);
        }
        serverUrlModified = false;
        httpPortTextField.getDocument().addDocumentListener(this);
        cardManagerUrlField.getDocument().addDocumentListener(this);
        serverUrlField.getDocument().addDocumentListener(this);
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.CustomizeDevice"); //NOI18N
    }

    private class ComboEditor implements ComboBoxEditor, DocumentListener {
        private final JTextField field = new JTextField();
        ComboEditor() {
            field.getDocument().addDocumentListener(this);
        }

        public Component getEditorComponent() {
            return field;
        }

        private boolean inSetItem;
        public void setItem(final Object value) {
            if (inSetItem) return;
            inSetItem = true;
            try {
                group.modifyComponents(new Runnable() {
                    public void run() {
                        try {
                            field.setText (value == null ? "" : value.toString()); //NOI18N
                        } catch (IllegalStateException e) {}
                    }
                });
                
            } finally {
                inSetItem = false;
            }
        }

        public Object getItem() {
            return field.getText();
        }

        public void selectAll() {
            field.selectAll();
        }

        public void addActionListener(ActionListener l) {
            //do nothing
        }

        public void removeActionListener(ActionListener l) {
            //do nothing
        }

        public void insertUpdate(DocumentEvent e) {
            hostComboBox.setSelectedItem(field.getText());
        }

        public void removeUpdate(DocumentEvent e) {
            insertUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
            insertUpdate(e);
        }
    }

    void initHostModel() {
        DefaultComboBoxModel mdl = new DefaultComboBoxModel();
        String hosts = NbPreferences.forModule(DevicePropertiesPanel.class).get("knownHosts", "localhost,127.0.0.1"); //NOI18N
        String[] h = hosts.split(",");
        boolean localhostFound = false;
        boolean oneTwentySevenFound = false;
        for (int i = 0; i < h.length; i++) {
            localhostFound |= "localhost".compareToIgnoreCase(h[i]) == 0; //NOI18N
            oneTwentySevenFound |="127.0.0.1".equals(h[i]); //NOI18N
            mdl.addElement(h[i]);
        }
        if (!localhostFound) {
            mdl.addElement("localhost"); //NOI18N
        }
        if (!oneTwentySevenFound) {
            mdl.addElement("127.0.0.1"); //NOI18N
        }
    }

    private static final void localizeName (Component c) {
        if (c.getName() != null) {
            c.setName (NbBundle.getMessage (DevicePropertiesPanel.class, c.getName()));
        }
    }

    public ValidationGroup getValidationGroup() {
        return group;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        group.validateAll();
    }

    public DevicePropertiesPanel() {
        initializeComponent();
    }

    String displayName = ""; //NOI18N
    public DevicePropertiesPanel(final Properties props) {
        initializeComponent();
        read (new KeysAndValues.PropertiesAdapter(props));
    }

    private void initializeComponent() {
        group.modifyComponents(this);
    }

    private class PortMismatchValidator implements Validator<String> {

        public boolean validate(Problems p, String compName, String model) {
            String httpPortString = httpPortTextField.getText();
            int portVal;
            try {
                portVal = Integer.parseInt (httpPortString.trim());
            } catch (NumberFormatException e) {
                return true;
            }
            int cmPort;
            int serverUrlPort;
            try {
                URL cmUrl = new URL (cardManagerUrlField.getText());
                URL servUrl = new URL (serverUrlField.getText());
                cmPort = cmUrl.getPort();
                serverUrlPort = servUrl.getPort();
            } catch (MalformedURLException e) {
                return true;
            }
            if (cmPort != portVal || serverUrlPort != portVal) {
                p.add (new Problem (NbBundle.getMessage(DevicePropertiesPanel.class,
                        "MSG_SERVER_PORT_PROBLEM", compName, model), Severity.WARNING)); //NOI18N
                return false;
            }
            return true;
        }
    }

    private class RemoteVsLocalhostMismatchValidator implements Validator <String> {

        public boolean validate(Problems pblms, String compName, String value) {
            boolean setToLocal = !remoteCheckbox.isSelected();
            boolean mismatch = (containsLocalhostReference(cardManagerUrlField.getText()) ||
                    containsLocalhostReference(serverUrlField.getText())) && !setToLocal;
            if (mismatch) {
                pblms.add(NbBundle.getMessage(RemoteVsLocalhostMismatchValidator.class,
                        "WARN_REMOTE_LOCAL_MISMATCH"), Severity.WARNING); //NOI18N
            }
            return !mismatch;
        }

        private boolean containsLocalhostReference (String val) {
            return val.indexOf ("127.0.0.1") >= 0 || //NOI18N
                    val.indexOf ("localhost") >= 0; //NOI18N
        }
    }

    private class PortOverlapValidator implements Validator <String> {

        public boolean validate(Problems problems, String arg1, String model) {
            Set<Integer> others = new HashSet<Integer>();
            JTextField[] relevant = new JTextField[] { httpPortTextField,
                proxy2cjcrePortTextField, proxy2idePortTextField,
                contactedPortTextField, contactlessPortTextField};
            for (int i = 0; i < relevant.length; i++) {
                String s = relevant[i].getText().trim();
                if (arg1.equals(relevant[i].getName())) {
                    continue;
                }
                try {
                    others.add (Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    //taken care of elsewhere
                }
            }
            String test = model.trim();
            try {
                Integer i = Integer.parseInt(test);
                boolean result = !others.contains (i);
                if (!result) {
                    problems.add (NbBundle.getMessage(DevicePropertiesPanel.class, 
                            "ERR_PORT_USED_TWICE", i)); //NOI18N
                }
                return result;
            } catch (NumberFormatException e) {
                //do nothing
                return true;
            }
        }

    }

    public void write(KeysAndValues<?> s) {
        s.put(DEVICE_RAMSIZE, getRAMSize());
        s.put(DEVICE_E2PSIZE, getE2PSize());
        s.put(DEVICE_CORSIZE, getCORSize());
        String port = getHTTPPort();
        s.put(DEVICE_HTTPPORT, port);
        s.put(DEVICE_PROXY2CJCREPORT, getProxy2cjcrePort());
        s.put(DEVICE_PROXY2IDEPORT, getProxy2idePort());
        s.put(DEVICE_CONTACTEDPORT, getContactedPort());
        s.put(DEVICE_CONTACTEDPROTOCOL, getContactedProtocol());
        //XXX what is this, anyway?
        s.put(DEVICE_APDUTOOL_CONTACTEDPROTOCOL, "T=0".equals(getContactedProtocol())? "-t0":""); //NOI18N
        s.put(DEVICE_CONTACTLESSPORT, getContactlessPort());
        s.put(DEVICE_LOGGERLEVEL, getLoggerLevel());
        s.put(DEVICE_SECUREMODE, getSecureMode());
        s.put(DEVICE_IS_REMOTE, remoteCheckbox.isSelected() + ""); //NOI18N
        s.put(DEVICE_SUSPEND_THREADS_ON_STARTUP, suspendCheckBox.isSelected() + ""); //NOI18N
        String host = hostComboBox.getSelectedItem().toString();
        s.put(DEVICE_HOST, host);
        NbPreferences.forModule(DevicePropertiesPanel.class).put("knownHosts",  //NOI18N
                getKnownHosts());
        String serverUrl = getServerUrl();
        String cardManagerUrl = getCardManagerUrl();
        try {
            //Try to substitute Ant-style properties for those things
            //that are listed elsewhere in the file
            URL url = new URL(serverUrl);
            URL curl = new URL(cardManagerUrl);
            StringBuilder usb = new StringBuilder(url.getProtocol());
            StringBuilder csb = new StringBuilder(curl.getProtocol());
            usb.append("://"); //NOi18N
            csb.append("://"); //NOI18N
            if (url.getHost().equalsIgnoreCase(host)) {
                usb.append ("${"); //NOI18N
                usb.append (DEVICE_HOST);
                usb.append ("}"); //NOI18N
            } else {
                usb.append(url.getHost());
            }
            if (curl.getHost().equalsIgnoreCase(host)) {
                csb.append("${"); //NOI18N
                csb.append(DEVICE_HOST);
                csb.append("}"); //NOI18N
            } else {
                csb.append(curl.getHost());
            }
            if (port.equals("" + url.getPort())) { //NOI18N
                usb.append(":${"); //NOI18N
                usb.append(DEVICE_HTTPPORT);
                usb.append('}'); //NOI18N
            } else {
                usb.append(':'); //NOI18N
                usb.append (url.getPort());
            }
            if (port.equals("" + curl.getPort())) { //NOI18N
                csb.append(":${"); //NOI18N
                csb.append(DEVICE_HTTPPORT);
                csb.append('}'); //NOI18N
            } else {
                csb.append(':'); //NOI18N
                csb.append(curl.getPort());
            }
            if (url.getPath() != null) {
                usb.append(url.getPath());
            }
            if (curl.getPath() != null) {
                csb.append(curl.getPath());
            }
            if (!usb.toString().endsWith("/")) { //NOI18N
                usb.append("/");
            }
            if (!csb.toString().endsWith("/")) { //NOI18N
                csb.append("/"); //NOI18N
            }
            serverUrl = usb.toString();
            cardManagerUrl = csb.toString();
        } catch (MalformedURLException ex) {
            s.put(DEVICE_SERVERURL, getServerUrl());
            s.put(DEVICE_CARDMANAGERURL, getCardManagerUrl());
            return;
        }
        s.put(DEVICE_SERVERURL, serverUrl);
        s.put(DEVICE_CARDMANAGERURL, cardManagerUrl);
    }

    private String getKnownHosts() {
        ComboBoxModel mdl = hostComboBox.getModel();
        StringBuilder sb = new StringBuilder();
        Set<String> s = new HashSet<String>();
        for (int i = 0; i < mdl.getSize(); i++) {
            String host = (String) mdl.getElementAt(mdl.getSize() - (i + 1));
            if ("localhost".compareToIgnoreCase(host) == 0 || "127.0.0.1".equals(host)) { //NOI18N
                continue;
            }
            if (sb.length() > 0) {
                sb.append(','); //NOI18N
            }
            sb.append(host);
            s.add(host);
        }
        if (!s.contains(mdl.getSelectedItem().toString())) {
            if (sb.length() > 0) {
                sb.append(','); //NOI18N
            }
            sb.append(mdl.getSelectedItem().toString().trim());
        }
        return sb.toString();
    }

    public void read(KeysAndValues<?> s) {
        updating = true;
        displayName = s.get(DEVICE_DISPLAY_NAME);
        try {
            String val = s.get(DEVICE_RAMSIZE);
            if (val != null) {
                ramSpinner.setValue(val);
            }
            val = s.get(DEVICE_E2PSIZE);
            if (val != null) {
                e2pSpinner.setValue(val);
            }
            val = s.get(DEVICE_CORSIZE);
            if (val != null) {
                corSpinner.setValue(val);
            }
            val = s.get(DEVICE_HOST);
            if (val != null) {
                hostComboBox.setSelectedItem(val);
                DefaultComboBoxModel mdl = (DefaultComboBoxModel) hostComboBox.getModel();
                if (mdl.getIndexOf(val) < 0) {
                    mdl.addElement(val);
                }
            }
            val = s.get(DEVICE_HTTPPORT);
            if (val != null) {
                httpPortTextField.setText(val);
            }
            val = s.get(DEVICE_PROXY2CJCREPORT);
            if (val != null) {
                proxy2cjcrePortTextField.setText(val);
            }
            val = s.get(DEVICE_PROXY2IDEPORT);
            if (val != null) {
                proxy2idePortTextField.setText(val);
            }
            val = s.get(DEVICE_CONTACTEDPORT);
            if (val != null) {
                contactedPortTextField.setText(val);
            }
            val = s.get(DEVICE_CONTACTEDPROTOCOL);
            if (val != null) {
                contactedProtocolComboBox.setSelectedItem(val);
            }
            val = s.get(DEVICE_CONTACTLESSPORT);
            if (val != null) {
                contactlessPortTextField.setText(s.get(DEVICE_CONTACTLESSPORT));
            }
            val = s.get(DEVICE_LOGGERLEVEL);
            if (val != null) {
                loggerLevelComboBox.setSelectedItem(val);
            }
            val = s.get(DEVICE_SECUREMODE);
            if (val != null) {
                secureModeCheckBox.setSelected(Boolean.valueOf(val));
            }
            val = s.get(DEVICE_SERVERURL);
            if (val != null) {
                serverUrlField.setText(val);
            }
            val = s.get(DEVICE_CARDMANAGERURL);
            if (val != null) {
                cardManagerUrlField.setText(val);
            }
            val = s.get(DEVICE_SUSPEND_THREADS_ON_STARTUP);
            if (val == null) {
                val = "false";
            }
            suspendCheckBox.setSelected(Boolean.valueOf(val));
            
            val = s.get(DEVICE_IS_REMOTE);
            if (val != null) {
                remoteCheckbox.setSelected(Boolean.valueOf(val));
            }

        } finally {
            updating = false;
            serverUrlModified = false;
        }
    }

    public String getRAMSize() {
        return ramSpinner.getValue().toString();
    }

    public String getE2PSize() {
        return e2pSpinner.getValue().toString();
    }

    public String getCORSize() {
        return corSpinner.getValue().toString();
    }

    public String getHTTPPort() {
        return httpPortTextField.getText().trim();
    }

    public String getProxy2cjcrePort() {
        return proxy2cjcrePortTextField.getText().trim();
    }

    public String getProxy2idePort() {
        return proxy2idePortTextField.getText().trim();
    }

    public String getContactedPort() {
        return contactedPortTextField.getText().trim();
    }

    public String getContactedProtocol() {
        return contactedProtocolComboBox.getSelectedItem().toString();
    }

    public String getContactlessPort() {
        return contactlessPortTextField.getText().trim();
    }

    public String getLoggerLevel() {
        return loggerLevelComboBox.getSelectedItem().toString();
    }

    public String getSecureMode() {
        return "" + secureModeCheckBox.isSelected();
    }

    public String getServerUrl() {
        return serverUrlField.getText();
    }

    public String getCardManagerUrl() {
        return cardManagerUrlField.getText();
    }

    public boolean isAllDataValid() {
        try {
            Problem p = group.validateAll();
            return p == null || !p.isFatal();
        } catch (StackOverflowError err) {
            //Stack overflow error in java.util.regex.Pattern - need to
            //diagnose further
            Logger.getLogger(DevicePropertiesPanel.class.getName()).log(Level.SEVERE,
                    null, err);
            return true;
        }
        
    }

    void setWizardDescriptor(WizardDescriptor wizardDescriptor) {
        group.addUI(new WizardDescriptorAdapter(wizardDescriptor));
        fireChange();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        secureModeCheckBox = new javax.swing.JCheckBox();
        loggerLevelComboBox = new javax.swing.JComboBox();
        corSpinner = new javax.swing.JSpinner();
        e2pSpinner = new javax.swing.JSpinner();
        ramSpinner = new javax.swing.JSpinner();
        ramSizeLabel = new javax.swing.JLabel();
        eepromSizeLabel = new javax.swing.JLabel();
        corSizeLabel = new javax.swing.JLabel();
        loggerLevelLabel = new javax.swing.JLabel();
        httpPortTextField = new javax.swing.JTextField();
        httpPortLabel = new javax.swing.JLabel();
        debugEmulatorLabel = new javax.swing.JLabel();
        proxy2cjcrePortTextField = new javax.swing.JTextField();
        suspendCheckBox = new javax.swing.JCheckBox();
        contactedPortTextField = new javax.swing.JTextField();
        contactlessPortTextField = new javax.swing.JTextField();
        debugInfoLabel = new javax.swing.JLabel();
        ideDebugLabel = new javax.swing.JLabel();
        proxy2idePortTextField = new javax.swing.JTextField();
        commInfoLabel = new javax.swing.JLabel();
        serverUrlLabel = new javax.swing.JLabel();
        serverUrlField = new javax.swing.JTextField();
        cardManagerUrlLabel = new javax.swing.JLabel();
        cardManagerUrlField = new javax.swing.JTextField();
        contactedProtocolLabel = new javax.swing.JLabel();
        contactedProtocolComboBox = new javax.swing.JComboBox();
        contactedPortLabel = new javax.swing.JLabel();
        contactlessPortLabel = new javax.swing.JLabel();
        remoteCheckbox = new javax.swing.JCheckBox();
        hostLabel = new javax.swing.JLabel();
        hostComboBox = new javax.swing.JComboBox();
        cardSettingsLabel = new javax.swing.JLabel();

        secureModeCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(secureModeCheckBox, org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("Run_In_Secure_Mode")); // NOI18N
        secureModeCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_bypass_security")); // NOI18N
        secureModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secureModeCheckBoxActionPerformed(evt);
            }
        });

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 12));
        setLayout(new java.awt.GridBagLayout());

        loggerLevelComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "fatal", "error", "warn", "info", "verbose", "debug", "all" }));
        loggerLevelComboBox.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("debuglevel_1")); // NOI18N
        loggerLevelComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerLevelComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 3, 0, 0);
        add(loggerLevelComboBox, gridBagConstraints);

        corSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"512", "1K", "2K", "4K"}));
        corSpinner.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_sizeofclear")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 110;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 3, 0, 0);
        add(corSpinner, gridBagConstraints);

        e2pSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"128K", "512K", "1M", "2M", "4M", "8M", "16M", "32M"}));
        e2pSpinner.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_persistent_memory_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 102;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 3, 0, 0);
        add(e2pSpinner, gridBagConstraints);

        ramSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"24K", "32K", "48K", "64K", "96K", "128K", "512K", "1M", "2M", "4M", "8M", "16M", "32M"}));
        ramSpinner.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ramsize_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 110;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(20, 3, 0, 0);
        add(ramSpinner, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ramSizeLabel, "RAM Size:");
        ramSizeLabel.setToolTipText("RAM size for this Instance.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 0);
        add(ramSizeLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(eepromSizeLabel, "EEPROM Size:");
        eepromSizeLabel.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_persistent_memory_size")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 0);
        add(eepromSizeLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(corSizeLabel, "COR Size:");
        corSizeLabel.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_clear_on_reset")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 0);
        add(corSizeLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(loggerLevelLabel, "Logger Level:");
        loggerLevelLabel.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_debug_level")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 0);
        add(loggerLevelLabel, gridBagConstraints);

        httpPortTextField.setText("8019");
        httpPortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_http_port_1")); // NOI18N
        httpPortTextField.setName("HTTP Port"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 20);
        add(httpPortTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(httpPortLabel, "HTTP Port:");
        httpPortLabel.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_http_port_2")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 12, 0, 0);
        add(httpPortLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(debugEmulatorLabel, "Debug Proxy <-> Emulator Port: ");
        debugEmulatorLabel.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_debug_port")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 20, 0, 0);
        add(debugEmulatorLabel, gridBagConstraints);

        proxy2cjcrePortTextField.setText("7019");
        proxy2cjcrePortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ide_port_1")); // NOI18N
        proxy2cjcrePortTextField.setName("Debug CJCRE Port"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(16, 10, 0, 0);
        add(proxy2cjcrePortTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(suspendCheckBox, "Suspend Threads on startup");
        suspendCheckBox.setToolTipText("<html>Check this checkbox if you want to start<br>stepping through code in the debugger<br>as soon as the card is started");
        suspendCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 20, 0, 0);
        add(suspendCheckBox, gridBagConstraints);

        contactedPortTextField.setText("9025");
        contactedPortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_apdu_port")); // NOI18N
        contactedPortTextField.setName("Contacted Port"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 20);
        add(contactedPortTextField, gridBagConstraints);

        contactlessPortTextField.setText("9026");
        contactlessPortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_APDU_Contactless_port")); // NOI18N
        contactlessPortTextField.setName("Contactless Port"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 20);
        add(contactlessPortTextField, gridBagConstraints);

        debugInfoLabel.setFont(debugInfoLabel.getFont().deriveFont(debugInfoLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(debugInfoLabel, "Debugger Info");
        debugInfoLabel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getDefaults().getColor("controlShadow")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 20);
        add(debugInfoLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ideDebugLabel, "IDE <-> Debug Proxy Port: ");
        ideDebugLabel.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ide_debug_proxy_port")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 0);
        add(ideDebugLabel, gridBagConstraints);

        proxy2idePortTextField.setText("7020");
        proxy2idePortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ide_debug_port_1")); // NOI18N
        proxy2idePortTextField.setName("IDE Debug Port"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
        add(proxy2idePortTextField, gridBagConstraints);

        commInfoLabel.setFont(commInfoLabel.getFont().deriveFont(commInfoLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(commInfoLabel, "Communication Info");
        commInfoLabel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getDefaults().getColor("controlShadow")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 20);
        add(commInfoLabel, gridBagConstraints);

        serverUrlLabel.setLabelFor(serverUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(serverUrlLabel, "Server URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 20, 0, 0);
        add(serverUrlLabel, gridBagConstraints);

        serverUrlField.setText("http://localhost:8019"); // NOI18N
        serverUrlField.setToolTipText("The general URL for interacting with this card");
        serverUrlField.setName("Server URL"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 20);
        add(serverUrlField, gridBagConstraints);

        cardManagerUrlLabel.setLabelFor(cardManagerUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(cardManagerUrlLabel, "Card Manager URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 20, 0, 0);
        add(cardManagerUrlLabel, gridBagConstraints);

        cardManagerUrlField.setText("http://localhost:8019/cardmanager"); // NOI18N
        cardManagerUrlField.setToolTipText("The URL used for deploying projects to this card");
        cardManagerUrlField.setName("Card Manager URL"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 20);
        add(cardManagerUrlField, gridBagConstraints);

        contactedProtocolLabel.setLabelFor(contactedProtocolComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(contactedProtocolLabel, "Contacted Protocol:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 12, 0, 0);
        add(contactedProtocolLabel, gridBagConstraints);

        contactedProtocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "T=0", "T=1" }));
        contactedProtocolComboBox.setToolTipText("Select the protocol to use on contacted port.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 20);
        add(contactedProtocolComboBox, gridBagConstraints);

        contactedPortLabel.setLabelFor(contactedPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(contactedPortLabel, "Contacted Port:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
        add(contactedPortLabel, gridBagConstraints);

        contactlessPortLabel.setLabelFor(contactlessPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(contactlessPortLabel, "Contactless Port:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(contactlessPortLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(remoteCheckbox, "Card Manager is on a remote computer");
        remoteCheckbox.setToolTipText("<html>If true, this card is not running on the same computer<br>as the IDE is");
        remoteCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        remoteCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onRemoteCheckboxChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 20, 20);
        add(remoteCheckbox, gridBagConstraints);

        hostLabel.setLabelFor(hostComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, "Host");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 0);
        add(hostLabel, gridBagConstraints);

        hostComboBox.setEditable(true);
        hostComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "localhost", "127.0.0.1" }));
        hostComboBox.setToolTipText("<html>The computer the IDE should communicate with<br>to talk to this Card");
        hostComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hostComboChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 20);
        add(hostComboBox, gridBagConstraints);

        cardSettingsLabel.setFont(cardSettingsLabel.getFont().deriveFont(cardSettingsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(cardSettingsLabel, "Card Settings");
        cardSettingsLabel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getDefaults().getColor("controlShadow")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 20);
        add(cardSettingsLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void secureModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secureModeCheckBoxActionPerformed
        fireChange();
}//GEN-LAST:event_secureModeCheckBoxActionPerformed

    private void loggerLevelComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerLevelComboBoxActionPerformed
        fireChange();
}//GEN-LAST:event_loggerLevelComboBoxActionPerformed

    boolean remoteBoxManuallyChanged = false;
    boolean inUpdate;
    private void onRemoteCheckboxChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onRemoteCheckboxChanged
        if (inUpdate) {
            return;
        }
        remoteBoxManuallyChanged = true;
        group.validateAll();
    }//GEN-LAST:event_onRemoteCheckboxChanged

    private void hostComboChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hostComboChanged
        insertUpdate(null);
    }//GEN-LAST:event_hostComboChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cardManagerUrlField;
    private javax.swing.JLabel cardManagerUrlLabel;
    private javax.swing.JLabel cardSettingsLabel;
    private javax.swing.JLabel commInfoLabel;
    private javax.swing.JLabel contactedPortLabel;
    private javax.swing.JTextField contactedPortTextField;
    private javax.swing.JComboBox contactedProtocolComboBox;
    private javax.swing.JLabel contactedProtocolLabel;
    private javax.swing.JLabel contactlessPortLabel;
    private javax.swing.JTextField contactlessPortTextField;
    private javax.swing.JLabel corSizeLabel;
    private javax.swing.JSpinner corSpinner;
    private javax.swing.JLabel debugEmulatorLabel;
    private javax.swing.JLabel debugInfoLabel;
    private javax.swing.JSpinner e2pSpinner;
    private javax.swing.JLabel eepromSizeLabel;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel httpPortLabel;
    private javax.swing.JTextField httpPortTextField;
    private javax.swing.JLabel ideDebugLabel;
    private javax.swing.JComboBox loggerLevelComboBox;
    private javax.swing.JLabel loggerLevelLabel;
    private javax.swing.JTextField proxy2cjcrePortTextField;
    private javax.swing.JTextField proxy2idePortTextField;
    private javax.swing.JLabel ramSizeLabel;
    private javax.swing.JSpinner ramSpinner;
    private javax.swing.JCheckBox remoteCheckbox;
    private javax.swing.JCheckBox secureModeCheckBox;
    private javax.swing.JTextField serverUrlField;
    private javax.swing.JLabel serverUrlLabel;
    private javax.swing.JCheckBox suspendCheckBox;
    // End of variables declaration//GEN-END:variables

    public void setCorSize(String value) {
        //Used in unit tests only
        corSpinner.setValue(value);
    }

    public boolean isSuspendThreads() {
        return suspendCheckBox.isSelected();
    }

    private boolean affectsServerURL(Object o) {
        Document hostEditorDocument = hostComboBox.getEditor().getEditorComponent() instanceof JTextField ?
            ((JTextField) hostComboBox.getEditor().getEditorComponent()).getDocument() : null;
        return o == null || o == httpPortTextField.getDocument() || o == hostComboBox ||
                (o != null && o == hostEditorDocument);
    }

    private boolean serverUrlModified;
    public void insertUpdate(DocumentEvent e) {
        serverUrlModified |= e != null && ((e.getDocument() == serverUrlField.getDocument() ||
                e.getDocument() == cardManagerUrlField.getDocument()));
        if (!serverUrlModified && affectsServerURL(e == null ? null : e.getDocument())) {
            Document hostEditorDocument = hostComboBox.getEditor().getEditorComponent() instanceof JTextField ?
                ((JTextField) hostComboBox.getEditor().getEditorComponent()).getDocument() : null;
            //XXX - update card manager url fields
            inUpdate = true;
            try {
                int port = Integer.parseInt (httpPortTextField.getText());
                String host = hostComboBox.getSelectedItem().toString().trim();
                URL sUrl = new URL (serverUrlField.getText());
                URL cUrl = new URL (cardManagerUrlField.getText());
                final URL newSurl = new URL (sUrl.getProtocol(), host, port, sUrl.getFile());
                final URL newCurl = new URL (cUrl.getProtocol(), host, port, cUrl.getFile());
                final boolean local = "localhost".compareToIgnoreCase(host) == 0 || "127.0.0.1".equals(host); //NOI18N
                group.modifyComponents(new Runnable() {
                    public void run() {
                        serverUrlField.setText (newSurl.toString());
                        cardManagerUrlField.setText(newCurl.toString());
                        serverUrlModified = false;
                        if (!remoteBoxManuallyChanged) {
                            remoteCheckbox.setSelected(!local);
                        }
                    }
                });
                group.validateAll();
            } catch (NumberFormatException ex) {
                //do nothing
            } catch (MalformedURLException ex) {
                //do nothing
            } finally {
                inUpdate = false;
            }
        }
    }

    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    public void focusGained(FocusEvent e) {
        ((JTextComponent) e.getComponent()).selectAll();
    }

    public void focusLost(FocusEvent e) {
        //do nothing
    }

    public void clearProblem() {
        fireChange();
    }

    public void setProblem(Problem p) {
        fireChange();
    }

    public void removeChangeListener(ChangeListener arg0) {
        supp.removeChangeListener(arg0);
    }

    public void fireChange() {
        if (!updating) {
            updating = true;
            try {
                supp.fireChange();
            } finally {
                updating = false;
            }
        }
    }

    public void addChangeListener(ChangeListener arg0) {
        supp.addChangeListener(arg0);
    }

    private static final class ProtocolRenderer implements ListCellRenderer {
        HtmlRenderer.Renderer ren = HtmlRenderer.createRenderer();

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ren.setHtml(true);
            value = NbBundle.getMessage (ProtocolRenderer.class, value.toString(), value);
            return ren.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static final Set<Integer> allPortsInUse() {
        Set<Integer> result = new HashSet<Integer>();
        for (FileObject platformFile : Utils.sfsFolderForRegisteredJavaPlatforms().getChildren()) {
            try {
                if (platformFile.getNameExt().endsWith(JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION) ||
                        DataObject.find(platformFile).getNodeDelegate().getLookup().lookup(JavacardPlatform.class) != null) {
                    JavacardPlatform pform = DataObject.find(platformFile).getNodeDelegate().getLookup().lookup(JavacardPlatform.class);
                    if (pform != null) {
                        for (Card card : pform.getCards().getCards(false)) {
                            PortProvider ports = card.getCapability(PortProvider.class);
                            if (ports != null) {
                                result.addAll(ports.getClaimedPorts());
                            }
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }
}
