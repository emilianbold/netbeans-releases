/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.javacard.platform;

import static org.netbeans.modules.javacard.constants.JavacardDeviceKeyNames.*;
import org.netbeans.modules.javacard.card.CardManager;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.Problems;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.javacard.GuiUtils;
import org.netbeans.modules.javacard.api.ValidationGroupProvider;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.conversion.Converter;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationUI;
import org.openide.awt.HtmlRenderer;

public class DevicePropertiesPanel extends JPanel implements DocumentListener, FocusListener, ValidationUI, Runnable, ValidationGroupProvider {

    private final ChangeSupport supp = new ChangeSupport(this);
    private boolean updating;
    private ValidationGroup group = ValidationGroup.create(this);

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
        e2pSpinner.setValue("4M"); //NOI18N
        corSpinner.setValue("4K"); //NOI18N
        loggerLevelComboBox.setSelectedItem(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("debug"));
        contactedProtocolComboBox.setSelectedItem("T=1");
        CardManager manager = CardManager.getDefault();

        // set some default display name
        for (int i = 8019; i < Integer.MAX_VALUE; i += 2) {
            String str = "" + i; //NOI18N
            if (!manager.serverPortExists(str)) {
                httpPortTextField.setText(str);
                break;
            }
        }

        int idePort = 0;
        for (int i = 7019; i < Integer.MAX_VALUE; i += 2) {
            String str = "" + i; //NOI18N
            idePort = i + 1;
            if (!manager.serverPortExists(str)) {
                proxy2cjcrePortTextField.setText(str);
                break;
            }
        }
        // always try ideport = cjcreport + 1
        for (int i = idePort; i < Integer.MAX_VALUE; i += 2) {
            String str = "" + i; //NOI18N
            if (!manager.serverPortExists(str)) {
                proxy2idePortTextField.setText(str);
                break;
            }
        }
        int cl = 0;
        for (int i = 9025; i < Integer.MAX_VALUE; i += 2) {
            cl = i + 1;
            String str = "" + i; //NOI18N
            if (!manager.serverPortExists(str)) {
                contactedPortTextField.setText(str);
                break;
            }
        }
        // always try contactless = contacted + 1
        for (int i = cl; i < Integer.MAX_VALUE; i += 2) {
            String str = "" + i; //NOI18N
            if (!manager.serverPortExists(str)) {
                contactlessPortTextField.setText(str);
                break;
            }
        }
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
        group.add (cardManagerUrlField, Validators.REQUIRE_NON_EMPTY_STRING.forString(false),
                Validators.MAY_NOT_START_WITH_DIGIT.forString(false), Validators.URL_MUST_BE_VALID.forString(false),
                new RemoteVsLocalhostMismatchValidator());
        group.add (serverUrlField, Validators.REQUIRE_NON_EMPTY_STRING.forString(false),
                Validators.MAY_NOT_START_WITH_DIGIT.forString(false), Validators.URL_MUST_BE_VALID.forString(false),
                new RemoteVsLocalhostMismatchValidator());
        Validator<Document> v = Converter.find(String.class, Document.class).convert(new PortMismatchValidator());
        group.add (httpPortTextField, v);
        group.add (serverUrlField, v);
        group.add (httpPortTextField, v);

        for (Component c : getComponents()) {
            localizeName (c);
        }
        serverUrlModified = false;
        httpPortTextField.getDocument().addDocumentListener(this);
        cardManagerUrlField.getDocument().addDocumentListener(this);
        serverUrlField.getDocument().addDocumentListener(this);
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

    String displayName = "";
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
                        "MSG_SERVER_PORT_PROBLEM", compName, model), Severity.WARNING));
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
                        "WARN_REMOTE_LOCAL_MISMATCH"), Severity.WARNING);
            }
            return !mismatch;
        }

        private boolean containsLocalhostReference (String val) {
            return val.indexOf ("127.0.0.1") >= 0 ||
                    val.indexOf ("localhost") >= 0;
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
        s.put(DEVICE_HTTPPORT, getHTTPPort());
        s.put(DEVICE_PROXY2CJCREPORT, getProxy2cjcrePort());
        s.put(DEVICE_PROXY2IDEPORT, getProxy2idePort());
        s.put(DEVICE_CONTACTEDPORT, getContactedPort());
        s.put(DEVICE_CONTACTEDPROTOCOL, getContactedProtocol());
        s.put(DEVICE_APDUTOOL_CONTACTEDPROTOCOL, "T=0".equals(getContactedProtocol())? "-t0":""); //NOI18N
        s.put(DEVICE_CONTACTLESSPORT, getContactlessPort());
        s.put(DEVICE_LOGGERLEVEL, getLoggerLevel());
        s.put(DEVICE_SECUREMODE, getSecureMode());
        s.put(DEVICE_SERVERURL, getServerUrl());
        s.put(DEVICE_CARDMANAGERURL, getCardManagerUrl());
        s.put(DEVICE_IS_REMOTE, remoteCheckbox.isSelected() + ""); //NOI18N
        s.put(DEVICE_DONT_SUSPEND_THREADS_ON_STARTUP, !suspendCheckBox.isSelected() + ""); //NOI18N
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
            val = s.get(DEVICE_DONT_SUSPEND_THREADS_ON_STARTUP);
            if (val != null) {
                suspendCheckBox.setSelected(Boolean.valueOf(val));
            }
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
        Problem p = group.validateAll();
        return p == null || !p.isFatal();
    }

    void setWizardDescriptor(WizardDescriptor wizardDescriptor) {
        group.addUI(new WizardDescriptorAdapter(wizardDescriptor));
        fireChange();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        secureModeCheckBox = new javax.swing.JCheckBox();
        loggerLevelComboBox = new javax.swing.JComboBox();
        corSpinner = new javax.swing.JSpinner();
        e2pSpinner = new javax.swing.JSpinner();
        ramSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        httpPortTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        proxy2cjcrePortTextField = new javax.swing.JTextField();
        suspendCheckBox = new javax.swing.JCheckBox();
        contactedPortTextField = new javax.swing.JTextField();
        contactlessPortTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        proxy2idePortTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        serverUrlLabel = new javax.swing.JLabel();
        serverUrlField = new javax.swing.JTextField();
        cardManagerUrlLabel = new javax.swing.JLabel();
        cardManagerUrlField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        contactedProtocolComboBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        remoteCheckbox = new javax.swing.JCheckBox();

        secureModeCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(secureModeCheckBox, org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("Run_In_Secure_Mode")); // NOI18N
        secureModeCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_bypass_security")); // NOI18N
        secureModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secureModeCheckBoxActionPerformed(evt);
            }
        });

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 12));

        loggerLevelComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "fatal", "error", "warn", "info", "verbose", "debug", "all" }));
        loggerLevelComboBox.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("debuglevel")); // NOI18N
        loggerLevelComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerLevelComboBoxActionPerformed(evt);
            }
        });

        corSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"512", "1K", "2K", "4K"}));
        corSpinner.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_sizeofclear")); // NOI18N

        e2pSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"128K", "512K", "1M", "2M", "4M", "8M", "16M", "32M"}));
        e2pSpinner.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_persistent_memory")); // NOI18N

        ramSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"24K", "32K", "48K", "64K", "96K", "128K", "512K", "1M", "2M", "4M", "8M", "16M", "32M"}));
        ramSpinner.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ramsize")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "RAM Size:");
        jLabel3.setToolTipText("RAM size for this Instance.");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "EEPROM Size:");
        jLabel4.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_persistent_memory_size")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "COR Size:");
        jLabel5.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_clear_on_reset")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, "Logger Level:");
        jLabel6.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_debug_level")); // NOI18N

        httpPortTextField.setText("8019");
        httpPortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_http_port")); // NOI18N
        httpPortTextField.setName("HTTP Port"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "HTTP Port:");
        jLabel7.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_http_port_2")); // NOI18N

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, "Debug Proxy <-> CJCRE Port: ");
        jLabel10.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_debug_port")); // NOI18N

        proxy2cjcrePortTextField.setText("7019");
        proxy2cjcrePortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ide_port")); // NOI18N
        proxy2cjcrePortTextField.setName("Debug CJCRE Port"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendCheckBox, "Suspend Threads on startup");

        contactedPortTextField.setText("9025");
        contactedPortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_apdu_port")); // NOI18N
        contactedPortTextField.setName("Contacted Port"); // NOI18N

        contactlessPortTextField.setText("9026");
        contactlessPortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_APDU_Contactless_port")); // NOI18N
        contactlessPortTextField.setName("Contactless Port"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, "Debugger Info");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, "IDE <-> Debug Proxy Port: ");
        jLabel11.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ide_debug_proxy_port")); // NOI18N

        proxy2idePortTextField.setText("7020");
        proxy2idePortTextField.setToolTipText(org.openide.util.NbBundle.getBundle(DevicePropertiesPanel.class).getString("tip_ide_debug_port")); // NOI18N
        proxy2idePortTextField.setName("IDE Debug Port"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Communication Info");

        serverUrlLabel.setLabelFor(serverUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(serverUrlLabel, "Server URL");

        serverUrlField.setText("http://localhost:8019"); // NOI18N
        serverUrlField.setName("Server URL"); // NOI18N

        cardManagerUrlLabel.setLabelFor(cardManagerUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(cardManagerUrlLabel, "Card Manager URL");

        cardManagerUrlField.setText("http://localhost:8019/cardmanager"); // NOI18N
        cardManagerUrlField.setName("Card Manager URL"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Contacted Protocol:");

        contactedProtocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "T=0", "T=1" }));
        contactedProtocolComboBox.setToolTipText("Select the protocol to use on contacted port.");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, "Contacted Port:");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, "Contactless Port:");

        org.openide.awt.Mnemonics.setLocalizedText(remoteCheckbox, "Card Manager is on a remote computer");
        remoteCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onRemoteCheckboxChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel3)
                                    .add(jLabel4))
                                .add(11, 11, 11)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(e2pSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                                    .add(ramSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel5)
                                    .add(jLabel6))
                                .add(14, 14, 14)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(corSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                    .add(loggerLevelComboBox, 0, 114, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel7))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(contactedProtocolComboBox, 0, 136, Short.MAX_VALUE)
                                    .add(httpPortTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                                    .add(contactedPortTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contactlessPortTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cardManagerUrlLabel)
                            .add(serverUrlLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(serverUrlField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .add(cardManagerUrlField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel11)
                            .add(jLabel10))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(proxy2cjcrePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(proxy2idePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(suspendCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 283, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(remoteCheckbox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel3)
                                    .add(ramSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel4)
                                    .add(e2pSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel5)
                                    .add(corSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel6)
                                    .add(loggerLevelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(httpPortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(jLabel7))
                                    .add(layout.createSequentialGroup()
                                        .add(32, 32, 32)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                            .add(jLabel8)
                                            .add(contactedPortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel2)
                                    .add(contactedProtocolComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(contactlessPortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel12))))
                        .add(15, 15, 15)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(proxy2cjcrePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(proxy2idePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(suspendCheckBox)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(13, 13, 13)
                                .add(jLabel1))
                            .add(layout.createSequentialGroup()
                                .add(19, 19, 19)
                                .add(jSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(serverUrlLabel)
                            .add(serverUrlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cardManagerUrlLabel)
                            .add(cardManagerUrlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(remoteCheckbox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void secureModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secureModeCheckBoxActionPerformed
        fireChange();
}//GEN-LAST:event_secureModeCheckBoxActionPerformed

    private void loggerLevelComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerLevelComboBoxActionPerformed
        fireChange();
}//GEN-LAST:event_loggerLevelComboBoxActionPerformed

    private void onRemoteCheckboxChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onRemoteCheckboxChanged
        group.validateAll();
    }//GEN-LAST:event_onRemoteCheckboxChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cardManagerUrlField;
    private javax.swing.JLabel cardManagerUrlLabel;
    private javax.swing.JTextField contactedPortTextField;
    private javax.swing.JComboBox contactedProtocolComboBox;
    private javax.swing.JTextField contactlessPortTextField;
    private javax.swing.JSpinner corSpinner;
    private javax.swing.JSpinner e2pSpinner;
    private javax.swing.JTextField httpPortTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JComboBox loggerLevelComboBox;
    private javax.swing.JTextField proxy2cjcrePortTextField;
    private javax.swing.JTextField proxy2idePortTextField;
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

    private boolean serverUrlModified;
    public void insertUpdate(DocumentEvent e) {
        serverUrlModified |= (e.getDocument() == serverUrlField.getDocument() ||
                e.getDocument() == cardManagerUrlField.getDocument());
        if (!serverUrlModified && e.getDocument() == httpPortTextField.getDocument()) {
            //XXX - update card manager url fields
            try {
                int port = Integer.parseInt (httpPortTextField.getText());
                URL sUrl = new URL (serverUrlField.getText());
                URL cUrl = new URL (cardManagerUrlField.getText());
                final URL newSurl = new URL (sUrl.getProtocol(), sUrl.getHost(), port, sUrl.getFile());
                final URL newCurl = new URL (cUrl.getProtocol(), cUrl.getHost(), port, cUrl.getFile());
                group.modifyComponents(new Runnable() {
                    public void run() {
                        serverUrlField.setText (newSurl.toString());
                        cardManagerUrlField.setText(newCurl.toString());
                        serverUrlModified = false;
                    }
                });
                group.validateAll();
            } catch (NumberFormatException ex) {
                //do nothing
            } catch (MalformedURLException ex) {
                //do nothing
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
            supp.fireChange();
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
}

