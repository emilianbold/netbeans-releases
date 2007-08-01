/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.project.ui.wizard;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk, Adam Sotona
 */
public class PlatformSelectionPanelGUI extends JPanel implements ActionListener {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(560, 350);
    
    private final DefaultComboBoxModel deviceModel;
    private final HashMap<String,J2MEPlatform.J2MEProfile> name2profile;
    private String reqCfg, reqProf;
    private TemplateWizard wiz;
    private int firstConfigWidth = -1;
    
    /** Creates new form CustomizerCompile */
    public PlatformSelectionPanelGUI() {
        name2profile = new HashMap<String,J2MEPlatform.J2MEProfile>();
        initComponents();
        initAccessibility();
        deviceModel = new DefaultComboBoxModel();
        jComboDevice.setModel(deviceModel);
        
        // Read defined platforms and all configurations, profiles and optional packages
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, null));
        Arrays.sort(platforms, new Comparator<JavaPlatform>() {
            public int compare(final JavaPlatform o1, final JavaPlatform o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        jComboBoxTarget.setModel(new DefaultComboBoxModel(platforms));
        HashMap<J2MEPlatform.J2MEProfile,J2MEPlatform.J2MEProfile> cfg = new HashMap<J2MEPlatform.J2MEProfile,J2MEPlatform.J2MEProfile>(), 
        	prof = new HashMap<J2MEPlatform.J2MEProfile,J2MEPlatform.J2MEProfile>();
        for( int i = 0; i < platforms.length; i++ ) {
            if (platforms[i] instanceof J2MEPlatform) {
                J2MEPlatform platform = (J2MEPlatform)platforms[i];
                Profile profiles[] = platform.getSpecification().getProfiles();
                for (int j=0; j<profiles.length; j++) {
                    if (profiles[j] instanceof J2MEPlatform.J2MEProfile) {
                        J2MEPlatform.J2MEProfile p = (J2MEPlatform.J2MEProfile)profiles[j];
                        if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(p.getType())) {
                            p = takeBetter(p, cfg.remove(p));
                            cfg.put(p, p);
                        } else if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(p.getType())) {
                            p = takeBetter(p, prof.remove(p));
                            prof.put(p, p);
                        }
                    }
                }
            }
        }
        J2MEPlatform.J2MEProfile arr[] = cfg.values().toArray(new J2MEPlatform.J2MEProfile[cfg.size()]);
        initAllConfigurations(arr);
        arr = prof.values().toArray(new J2MEPlatform.J2MEProfile[prof.size()]);
        initAllProfiles(arr);
        jComboBoxTarget.addActionListener(this);
        jComboDevice.addActionListener(this);
        
    }
    
    private J2MEPlatform.J2MEProfile takeBetter(final J2MEPlatform.J2MEProfile p1, final J2MEPlatform.J2MEProfile p2) {
        if (p1 == null) return p2;
        if (p2 == null) return p1;
        return p1.getDisplayNameWithVersion().length() > p2.getDisplayNameWithVersion().length() ? p1 : p2;
    }
    
    private void initAllConfigurations(final J2MEPlatform.J2MEProfile cfgs[]) {
        Arrays.sort(cfgs);
        for (int i=0; i<cfgs.length; i++) {
            final JRadioButton btn = new JRadioButton(cfgs[i].toString()); // TO DO some text formating
            btn.setToolTipText(cfgs[i].getDisplayNameWithVersion());
            btn.setActionCommand(cfgs[i].toString());
            cfgGroup.add(btn);
            jPanelConfig.add(btn);
            final Dimension preferredSize = btn.getPreferredSize();
            if (firstConfigWidth == -1){
                firstConfigWidth = preferredSize.width + 30;
                btn.setPreferredSize(new java.awt.Dimension(firstConfigWidth, preferredSize.height));
            } else {
                btn.setPreferredSize(new java.awt.Dimension(firstConfigWidth, preferredSize.height));
            }
            btn.addActionListener(this);
        }
    }
    
    private void initAllProfiles(final J2MEPlatform.J2MEProfile profs[]) {
        Arrays.sort(profs);
        for (int i=0; i<profs.length; i++) {
            final JRadioButton btn = new JRadioButton(profs[i].toString()); // TO DO some text formating
            btn.setToolTipText(profs[i].getDisplayNameWithVersion());
            btn.setActionCommand(profs[i].toString());
            profGroup.add(btn);
            btn.setPreferredSize(new java.awt.Dimension(firstConfigWidth, btn.getPreferredSize().height));
            jPanelProfile.add(btn);
            btn.addActionListener(this);
        }
    }
    
    public synchronized void setValues(final TemplateWizard wiz, final J2MEPlatform platform, final J2MEPlatform.Device device, final String config, final String profile) {
        this.wiz = wiz;
        reqCfg = config;
        reqProf = profile;
        jComboBoxTarget.setSelectedItem(platform);
        updateDevices(device, config, profile);
    }
    
    private synchronized void updateDevices(final J2MEPlatform.Device device, final String config, final String profile) {
        final J2MEPlatform platform = getPlatform();
        String deviceName = device == null ? null : device.getName();
        if (deviceName == null ) {
            final J2MEPlatform.Device oldDevice = getDevice();
            if (oldDevice != null) deviceName = oldDevice.getName();
        }
        deviceModel.setSelectedItem(null);
        deviceModel.removeAllElements();
        if (platform != null) {
            final J2MEPlatform.Device devices[] = platform.getDevices();
            for (int i=0; i<devices.length; i++) {
                deviceModel.addElement(devices[i]);
                if (devices[i].getName().equals(deviceName)) deviceModel.setSelectedItem(devices[i]);
            }
            if (deviceModel.getSelectedItem() == null && devices.length > 0) deviceModel.setSelectedItem(devices[0]);
        }
        updateConfigsAndProfiles(config, profile);
    }
    
    private synchronized void updateConfigsAndProfiles(final String config, final String profile) {
        final J2MEPlatform.Device device = getDevice();
        String defCfg = null, defProf = null;
        name2profile.clear();
        if (device != null) {
            final J2MEPlatform.J2MEProfile p[] = device.getProfiles();
            for (int i=0; i<p.length; i++) {
                name2profile.put(p[i].toString(), p[i]);
                if (p[i].isDefault()) {
                    if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(p[i].getType())) {
                        defCfg = p[i].toString();
                    } else if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(p[i].getType())) {
                        defProf = p[i].toString();
                    }
                }
            }
        }
        updateGroup(cfgGroup, config, name2profile.keySet(), defCfg);
        updateGroup(profGroup, profile, name2profile.keySet(), defProf);
        updateErrorMessage();
    }
    
    private void updateGroup(final ButtonGroup grp, final String selected, final Set<String> enabled, final String def) {
        final Enumeration en = grp.getElements();
        JRadioButton defB = null;
        while (en.hasMoreElements()) {
            final JRadioButton btn = (JRadioButton)en.nextElement();
            final String name = btn.getActionCommand();
            btn.setEnabled(enabled.contains(name));
            if (selected != null && selected.equals(name)) grp.setSelected(btn.getModel(), true);
            if (def != null && def.equals(name)) defB = btn;
        }
        final ButtonModel m = grp.getSelection();
        if ((m == null || !m.isEnabled()) && defB != null) grp.setSelected(defB.getModel(), true);
    }
    
    public void updateErrorMessage() {
        final boolean cfgError = reqCfg != null  &&  !name2profile.containsKey(reqCfg);
        final boolean profError = reqProf != null  &&  !name2profile.containsKey(reqProf);
        if (wiz != null) {
            String message = null;
            if (cfgError && profError) {
                message = NbBundle.getMessage(PlatformSelectionPanelGUI.class, "ERR_PlatformSelection_Platform_does_not_support_2", reqCfg, reqProf); //NOI18N
            } else if (cfgError) {
                message = NbBundle.getMessage(PlatformSelectionPanelGUI.class, "ERR_PlatformSelection_Platform_does_not_support_1", reqCfg); //NOI18N
            } else if (profError) {
                message = NbBundle.getMessage(PlatformSelectionPanelGUI.class, "ERR_PlatformSelection_Platform_does_not_support_1", reqProf); //NOI18N
            }
            wiz.putProperty("WizardPanel_errorMessage", message); //NOI18N
        }
    }
    
    public J2MEPlatform getPlatform() {
        return (J2MEPlatform)jComboBoxTarget.getSelectedItem();
    }
    
    public J2MEPlatform.Device getDevice() {
        return (J2MEPlatform.Device)deviceModel.getSelectedItem();
    }
    
    public J2MEPlatform.J2MEProfile getConfiguration() {
        final ButtonModel m = cfgGroup.getSelection();
        return m == null ? null : name2profile.get(m.getActionCommand());
    }
    
    public J2MEPlatform.J2MEProfile getProfile() {
        final ButtonModel m = profGroup.getSelection();
        return m == null ? null : name2profile.get(m.getActionCommand());
    }
    
    public void actionPerformed(final ActionEvent e) {
        if (jComboBoxTarget.equals(e.getSource())) {
            updateDevices(null, null, null);
        } else if  (jComboDevice.equals(e.getSource())) {
            updateConfigsAndProfiles(null, null);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cfgGroup = new javax.swing.ButtonGroup();
        profGroup = new javax.swing.ButtonGroup();
        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelDevice = new javax.swing.JLabel();
        jComboDevice = new javax.swing.JComboBox();
        jLabelConfig = new javax.swing.JLabel();
        jPanelConfig = new javax.swing.JPanel();
        jLabelProfile = new javax.swing.JLabel();
        jPanelProfile = new javax.swing.JPanel();

        setName(org.openide.util.NbBundle.getMessage(PlatformSelectionPanelGUI.class, "TITLE_PlatformSelection")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jLabelTarget.setLabelFor(jComboBoxTarget);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTarget, NbBundle.getMessage(PlatformSelectionPanelGUI.class, "LBL_PlatformSelection_TargetPlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabelTarget, gridBagConstraints);
        jLabelTarget.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformSelectionPanelGUI.class, "ACSD_PlatSel_Platform")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jComboBoxTarget, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        jLabelDevice.setLabelFor(jComboDevice);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDevice, NbBundle.getMessage(PlatformSelectionPanelGUI.class, "LBL_PlatformSelection_Device")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelDevice, gridBagConstraints);
        jLabelDevice.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformSelectionPanelGUI.class, "ACSD_PlatSel_Device")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(jComboDevice, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelConfig, NbBundle.getMessage(PlatformSelectionPanelGUI.class, "LBL_PlatformSelection_DeviceConfiguration")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelConfig, gridBagConstraints);

        jPanelConfig.setLayout(new java.awt.GridLayout(1, 0, 5, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(jPanelConfig, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelProfile, NbBundle.getMessage(PlatformSelectionPanelGUI.class, "LBL_PlatformSelection_DeviceProfile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jLabelProfile, gridBagConstraints);

        jPanelProfile.setLayout(new java.awt.GridLayout(1, 0, 5, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 12, 0);
        add(jPanelProfile, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(PlatformSelectionPanelGUI.class, "ACSN_PlatformSelection"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformSelectionPanelGUI.class, "ACSD_PlatformSelection"));
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup cfgGroup;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JComboBox jComboDevice;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JLabel jLabelDevice;
    private javax.swing.JLabel jLabelProfile;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JPanel jPanelProfile;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.ButtonGroup profGroup;
    // End of variables declaration//GEN-END:variables
    
    
    
}
