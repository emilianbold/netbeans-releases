/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Roman Svitanic
 */
public class J2MERunPanel extends javax.swing.JPanel {

    private DataSource[] data;
    private Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> configs;
    private J2MEProjectProperties uiProperties;

    /**
     * Creates new form J2MERunPanel
     */
    public J2MERunPanel(J2MEProjectProperties properties) {
        initComponents();

        standardRadio.setActionCommand("STANDARD"); // NOI18N
        OTARadio.setActionCommand("OTA"); // NOI18N                

        this.uiProperties = properties;
        configs = uiProperties.RUN_CONFIGS;
        domainsCombo.setModel(new DefaultComboBoxModel(uiProperties.SECURITY_DOMAINS));


        data = new DataSource[]{
            new TextDataSource(ProjectProperties.APPLICATION_ARGS, labelCommandlineOptions, texfieldCmdOptions, configCombo, configs),
            new TextDataSource(J2MEProjectProperties.PROP_DEBUGGER_TIMEOUT, debugTimeoutLabel, debugTimeoutField, configCombo, configs),
            new ToggleButtonDataSource(J2MEProjectProperties.PROP_RUN_METHOD, standardRadio, configCombo, configs),
            new ToggleButtonDataSource(J2MEProjectProperties.PROP_RUN_METHOD, OTARadio, configCombo, configs),
            new ToggleButtonDataSource(J2MEProjectProperties.PROP_USE_SECURITY_DOMAIN, jCheckBoxUseSecurity, configCombo, configs),
            new ComboDataSource(J2MEProjectProperties.PROP_SECURITY_DOMAIN, domainsCombo, configCombo, configs)
        };

        configChanged(uiProperties.activeConfig);
        configCombo.setRenderer(new ConfigListCellRenderer());
        domainsCombo.setEnabled(jCheckBoxUseSecurity.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupRun = new javax.swing.ButtonGroup();
        configPanel = new javax.swing.JPanel();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        configNew = new javax.swing.JButton();
        configDel = new javax.swing.JButton();
        configSeparator = new javax.swing.JSeparator();
        panelJ2MERunOptions = new javax.swing.JPanel();
        labelCommandlineOptions = new javax.swing.JLabel();
        texfieldCmdOptions = new javax.swing.JTextField();
        standardRadio = new javax.swing.JRadioButton();
        jCheckBoxUseSecurity = new javax.swing.JCheckBox();
        domainsCombo = new javax.swing.JComboBox();
        OTARadio = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        debugTimeoutLabel = new javax.swing.JLabel();
        debugTimeoutField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        extPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        configPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.configLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        configPanel.add(configLabel, gridBagConstraints);

        configCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default>" }));
        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.configNew.text")); // NOI18N
        configNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configNew, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.configDel.text")); // NOI18N
        configDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configDel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(configPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(configSeparator, gridBagConstraints);

        panelJ2MERunOptions.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labelCommandlineOptions, NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.labelCommandlineOptions.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 5);
        panelJ2MERunOptions.add(labelCommandlineOptions, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        panelJ2MERunOptions.add(texfieldCmdOptions, gridBagConstraints);

        buttonGroupRun.add(standardRadio);
        org.openide.awt.Mnemonics.setLocalizedText(standardRadio, NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.standardRadio.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        panelJ2MERunOptions.add(standardRadio, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxUseSecurity, NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.jCheckBoxUseSecurity.text")); // NOI18N
        jCheckBoxUseSecurity.setActionCommand(NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.jCheckBoxUseSecurity.actionCommand")); // NOI18N
        jCheckBoxUseSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseSecurityActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        panelJ2MERunOptions.add(jCheckBoxUseSecurity, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        panelJ2MERunOptions.add(domainsCombo, gridBagConstraints);

        buttonGroupRun.add(OTARadio);
        org.openide.awt.Mnemonics.setLocalizedText(OTARadio, NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.OTARadio.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panelJ2MERunOptions.add(OTARadio, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        panelJ2MERunOptions.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panelJ2MERunOptions.add(jSeparator1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(debugTimeoutLabel, org.openide.util.NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.debugTimeoutLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 8, 5);
        panelJ2MERunOptions.add(debugTimeoutLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 5, 0);
        panelJ2MERunOptions.add(debugTimeoutField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panelJ2MERunOptions.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(panelJ2MERunOptions, gridBagConstraints);

        extPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(extPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        String config = (String) configCombo.getSelectedItem();
        if (config.length() == 0) {
            config = null;
        }
        configChanged(config);
        uiProperties.activeConfig = config;
    }//GEN-LAST:event_configComboActionPerformed

    private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
        createNewConfiguration();
    }//GEN-LAST:event_configNewActionPerformed

    private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
        String config = (String) configCombo.getSelectedItem();
        assert config != null;
        configs.put(config, null);
        configChanged(null);
        uiProperties.activeConfig = null;
    }//GEN-LAST:event_configDelActionPerformed

    private void jCheckBoxUseSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseSecurityActionPerformed
        domainsCombo.setEnabled(jCheckBoxUseSecurity.isSelected());
    }//GEN-LAST:event_jCheckBoxUseSecurityActionPerformed

    private void configChanged(String activeConfig) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("");
        SortedSet<String> alphaConfigs = new TreeSet<>(new Comparator<String>() {
            Collator coll = Collator.getInstance();

            @Override
            public int compare(String s1, String s2) {
                return coll.compare(label(s1), label(s2));
            }

            private String label(String c) {
                Map<String, String> m = configs.get(c);
                String label = m.get("$label"); // NOI18N
                return label != null ? label : c;
            }
        });
        for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config != null && entry.getValue() != null) {
                alphaConfigs.add(config);
            }
        }
        for (String c : alphaConfigs) {
            model.addElement(c);
        }
        configCombo.setModel(model);
        configCombo.setSelectedItem(activeConfig != null ? activeConfig : "");
        Map<String, String> m = configs.get(activeConfig);
        if (m != null) {
            for (DataSource ds : data) {
                ds.update(activeConfig);
            }
        }
        configDel.setEnabled(activeConfig != null);
        domainsCombo.setEnabled(jCheckBoxUseSecurity.isSelected());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton OTARadio;
    private javax.swing.ButtonGroup buttonGroupRun;
    private javax.swing.JComboBox configCombo;
    private javax.swing.JButton configDel;
    private javax.swing.JLabel configLabel;
    private javax.swing.JButton configNew;
    private javax.swing.JPanel configPanel;
    private javax.swing.JSeparator configSeparator;
    private javax.swing.JTextField debugTimeoutField;
    private javax.swing.JLabel debugTimeoutLabel;
    private javax.swing.JComboBox domainsCombo;
    private javax.swing.JPanel extPanel;
    private javax.swing.JCheckBox jCheckBoxUseSecurity;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelCommandlineOptions;
    private javax.swing.JPanel panelJ2MERunOptions;
    private javax.swing.JRadioButton standardRadio;
    private javax.swing.JTextField texfieldCmdOptions;
    // End of variables declaration//GEN-END:variables

    private void createNewConfiguration() {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.input.prompt"), // NOI18N
                NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.input.title")); // NOI18N
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String name = d.getInputText();
        String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
        if (config.trim().length() == 0) {
            //#143764
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.input.empty", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;

        }
        if (configs.get(config) != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2MERunPanel.class, "J2MERunPanel.input.duplicate", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        Map<String, String> m = new HashMap<>();
        if (!name.equals(config)) {
            m.put("$label", name); // NOI18N
        }
        configs.put(config, m);
        configChanged(config);
        uiProperties.activeConfig = config;
    }

    private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer, UIResource {

        public ConfigListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            String config = (String) value;
            String label;
            if (config == null) {
                // uninitialized?
                label = null;
            } else if (config.length() > 0) {
                Map<String, String> m = configs.get(config);
                label = m != null ? m.get("$label") : /* temporary? */ null; // NOI18N
                if (label == null) {
                    label = config;
                }
            } else {
                label = NbBundle.getBundle("org.netbeans.modules.java.j2seproject.Bundle").getString("J2SEConfigurationProvider.default.label"); // NOI18N
            }
            setText(label);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }

    private abstract static class DataSource {

        private final String propName;
        private JComponent label;
        private final JComboBox<?> configCombo;
        private final Map<String, Map<String, String>> configs;
        private final Font basefont;
        private final Font boldfont;

        DataSource(
                @NonNull final String propName,
                @NonNull final JComponent label,
                @NonNull final JComboBox<?> configCombo,
                @NonNull final Map<String, Map<String, String>> configs) {
            Parameters.notNull("propName", propName);   //NOI18N
            Parameters.notNull("label", label);         //NOI18N
            Parameters.notNull("configCombo", configCombo); //NOI18N
            Parameters.notNull("configs", configs); //NOI18N
            this.propName = propName;
            this.label = label;
            this.configCombo = configCombo;
            this.configs = configs;
            basefont = label.getFont();
            boldfont = basefont.deriveFont(Font.BOLD);
        }

        final String getPropertyName() {
            return propName;
        }

        final JComponent getLabel() {
            return label;
        }

        final void changed(@NullAllowed String value) {
            String config = (String) configCombo.getSelectedItem();
            if (config.length() == 0) {
                config = null;
            }
            if (value != null && config != null && value.equals(configs.get(null).get(propName))) {
                // default value, do not store as such
                value = null;
            }
            configs.get(config).put(propName, value);
            //updateFont(value);
        }

        final void updateFont(@NullAllowed String value) {
            String config = (String) configCombo.getSelectedItem();
            if (config.length() == 0) {
                config = null;
            }
            String def = configs.get(null).get(propName);
            label.setFont(config != null && !Utilities.compareObjects(
                    value != null ? value : "", def != null ? def : "") ? boldfont : basefont);
        }

        @CheckForNull
        final String getPropertyValue(
                @NullAllowed String config,
                @NonNull String key) {
            final Map<String, String> m = configs.get(config);
            String v = m.get(key);
            if (v == null) {
                // display default value
                final Map<String, String> def = configs.get(null);
                v = def.get(getPropertyName());
            }
            return v;
        }

        abstract String getPropertyValue();

        abstract void update(@NullAllowed String activeConfig);
    }

    private static class TextDataSource extends DataSource {

        private final JTextComponent textComp;

        TextDataSource(
                @NonNull final String propName,
                @NonNull final JLabel label,
                @NonNull final JTextComponent textComp,
                @NonNull final JComboBox<?> configCombo,
                @NonNull final Map<String, Map<String, String>> configs) {
            super(propName, label, configCombo, configs);
            Parameters.notNull("textComp", textComp);   //NOI18N
            this.textComp = textComp;
            this.textComp.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changed(textComp.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    changed(textComp.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });
        }

        @Override
        String getPropertyValue() {
            return textComp.getText();
        }

        @Override
        void update(@NullAllowed final String activeConfig) {
            textComp.setText(getPropertyValue(activeConfig, getPropertyName()));
        }
    }

    private static class ToggleButtonDataSource extends DataSource {

        private final JToggleButton toggleButton;

        public ToggleButtonDataSource(@NonNull final String propName,
                @NonNull final JToggleButton toggleButton,
                @NonNull final JComboBox<?> configCombo,
                @NonNull final Map<String, Map<String, String>> configs) {
            super(propName, toggleButton, configCombo, configs);
            Parameters.notNull("toggleButton", toggleButton); //NOI18N
            this.toggleButton = toggleButton;
            toggleButton.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (toggleButton instanceof JRadioButton && !toggleButton.isSelected()) {
                        return;
                    }
                    changed(readValue());
                }
            });
        }

        @Override
        String getPropertyValue() {
            return readValue();
        }

        @Override
        void update(String activeConfig) {
            String prop = getPropertyValue(activeConfig, getPropertyName());
            if (toggleButton instanceof JCheckBox) {
                toggleButton.setSelected(prop != null && Boolean.valueOf(prop));
            } else if (toggleButton instanceof JRadioButton) {
                if (prop == null || prop.isEmpty()) {
                    prop = "STANDARD"; //NOI18N
                }
                toggleButton.setSelected(prop.equals(toggleButton.getActionCommand()));
            }
        }

        final String readValue() {
            String value = ""; //NOI18N
            if (toggleButton instanceof JRadioButton) {
                value = toggleButton.isSelected() ? toggleButton.getActionCommand() : null; //NOI18N
            } else if (toggleButton instanceof JCheckBox) {
                value = String.valueOf(toggleButton.isSelected());
            }
            return value;
        }
    }

    private static class ComboDataSource extends DataSource {

        private final JComboBox<String> combo;

        ComboDataSource(
                @NonNull final String propName,
                @NonNull final JComboBox<String> combo,
                @NonNull final JComboBox<?> configCombo,
                @NonNull final Map<String, Map<String, String>> configs) {
            super(propName, combo, configCombo, configs);
            Parameters.notNull("combo", combo); //NOI18N
            this.combo = combo;
            this.combo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    changed(getPropertyValue());
                }
            });
        }

        @Override
        final String getPropertyValue() {
            return (String) combo.getSelectedItem();
        }

        @Override
        void update(String activeConfig) {
            String domainName = getPropertyValue(activeConfig, getPropertyName());
            if (domainName == null) {
                domainName = "";   //NOI18N
            }
            final ComboBoxModel<String> model = combo.getModel();

            for (int i = 0; i < model.getSize(); i++) {
                final String itemAt = model.getElementAt(i);
                if (domainName.equals(itemAt)) {
                    combo.setSelectedItem(itemAt);
                    return;
                }
            }
            combo.setSelectedIndex(0);
        }
    }
}
