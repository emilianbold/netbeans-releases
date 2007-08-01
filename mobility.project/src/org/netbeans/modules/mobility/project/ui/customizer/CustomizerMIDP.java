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

package org.netbeans.modules.mobility.project.ui.customizer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk, Adam Sotona
 */
final public class CustomizerMIDP extends JPanel implements CustomizerPanel, VisualPropertyGroup, ActionListener {
    
    private static final String[] PROPERTY_GROUP = new String [] {DefaultPropertiesDescriptor.PLATFORM_ACTIVE,
    DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION,
    DefaultPropertiesDescriptor.PLATFORM_DEVICE,
    DefaultPropertiesDescriptor.PLATFORM_CONFIGURATION,
    DefaultPropertiesDescriptor.PLATFORM_PROFILE,
    DefaultPropertiesDescriptor.PLATFORM_APIS,
    DefaultPropertiesDescriptor.PLATFORM_BOOTCLASSPATH,
    DefaultPropertiesDescriptor.JAVAC_SOURCE,
    DefaultPropertiesDescriptor.JAVAC_TARGET};
    
    
    private static final Comparator<J2MEPlatform.J2MEProfile> OPTIONAL_API_COMPARATOR = new Comparator<J2MEPlatform.J2MEProfile>() {
        public int compare(final J2MEPlatform.J2MEProfile o1, final J2MEPlatform.J2MEProfile o2) {
            return o1.getDisplayNameWithVersion().compareTo(o2.getDisplayNameWithVersion());
        }
    };
    
    private Map<String, Object> props;
    private VisualPropertySupport vps;
    private String configuration;
    private String platformNames[];
    private HashMap<String,J2MEPlatform> name2platform;
    private HashMap<String,J2MEPlatform.Device> name2device;
    private HashMap<String,J2MEPlatform.J2MEProfile> name2profile;
    private ArrayList<JCheckBox> optional;
    private boolean useDefault;
    
    /** Creates new form CustomizerCompile */
    public CustomizerMIDP() {
        initComponents();
        initAccessibility();
        initAll();
    }
    
    private synchronized void refreshAll() {
        jPanelConfig.removeAll();
        jPanelProfile.removeAll();
        jPanelOptional.removeAll();
        cfgGroup = new ButtonGroup();
        profGroup = new ButtonGroup();
        initAll();
        initGroupValues(useDefault);
        jPanelConfig.invalidate();
        jPanelProfile.invalidate();
        jPanelOptional.invalidate();
    }
    
    private synchronized void initAll() {
        optional = new ArrayList<JCheckBox>();
        name2platform = new HashMap<String,J2MEPlatform>();
        name2device = new HashMap<String,J2MEPlatform.Device>();
        name2profile = new HashMap<String,J2MEPlatform.J2MEProfile>();
        
        // Read defined platforms and all configurations, profiles and optional packages
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, null));
        final HashMap<J2MEPlatform.J2MEProfile,J2MEPlatform.J2MEProfile> cfg = new HashMap<J2MEPlatform.J2MEProfile,J2MEPlatform.J2MEProfile>(), 
        	prof = new HashMap<J2MEPlatform.J2MEProfile,J2MEPlatform.J2MEProfile>(), 
        	opt = new HashMap<J2MEPlatform.J2MEProfile,J2MEPlatform.J2MEProfile>();
        for( int i = 0; i < platforms.length; i++ ) {
            if (platforms[i] instanceof J2MEPlatform) {
                final J2MEPlatform platform = (J2MEPlatform)platforms[i];
                if (platform.isValid()) {
                    name2platform.put(platform.getDisplayName(), platform);
                    final Profile profiles[] = platform.getSpecification().getProfiles();
                    for (int j=0; j<profiles.length; j++) {
                        if (profiles[j] instanceof J2MEPlatform.J2MEProfile) {
                            J2MEPlatform.J2MEProfile p = (J2MEPlatform.J2MEProfile)profiles[j];
                            if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(p.getType())) {
                                p = takeBetter(p, cfg.remove(p));
                                cfg.put(p, p);
                            } else if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(p.getType())) {
                                p = takeBetter(p, prof.remove(p));
                                prof.put(p, p);
                            } else if (J2MEPlatform.J2MEProfile.TYPE_OPTIONAL.equals(p.getType())) {
                                p = takeBetter(p, opt.remove(p));
                                opt.put(p, p);
                            }
                        }
                    }
                }
            }
        }
        platformNames = name2platform.keySet().toArray(new String[name2platform.size()]);
        Arrays.sort(platformNames);
        J2MEPlatform.J2MEProfile arr[] = cfg.values().toArray(new J2MEPlatform.J2MEProfile[cfg.size()]);
        initConfigurations(arr);
        arr = prof.values().toArray(new J2MEPlatform.J2MEProfile[prof.size()]);
        initProfiles(arr);
        arr = opt.values().toArray(new J2MEPlatform.J2MEProfile[opt.size()]);
        initOptional(arr);
        
    }
    
    private J2MEPlatform.J2MEProfile takeBetter(final J2MEPlatform.J2MEProfile p1, final J2MEPlatform.J2MEProfile p2) {
        if (p1 == null) return p2;
        if (p2 == null) return p1;
        return p1.getDisplayNameWithVersion().length() > p2.getDisplayNameWithVersion().length() ? p1 : p2;
    }
    
    private void initConfigurations(final J2MEPlatform.J2MEProfile cfgs[]) {
        Arrays.sort(cfgs);
        for (int i=0; i<cfgs.length; i++) {
            final JRadioButton btn = new JRadioButton(cfgs[i].toString()); // TO DO some text formating
            btn.setToolTipText(cfgs[i].getDisplayNameWithVersion());
            btn.setActionCommand(cfgs[i].toString());
            cfgGroup.add(btn);
            jPanelConfig.add(btn);
            final Dimension preferredSize = btn.getPreferredSize();
            if (firstConfigWidth == -1){
                firstConfigWidth = preferredSize.width + 10;
                btn.setPreferredSize(new java.awt.Dimension(firstConfigWidth, preferredSize.height));
                btn.setMinimumSize(new java.awt.Dimension(firstConfigWidth, preferredSize.height));
            } else {
                btn.setPreferredSize(new java.awt.Dimension(firstConfigWidth, preferredSize.height));
                btn.setMinimumSize(new java.awt.Dimension(firstConfigWidth, preferredSize.height));
            }
            btn.addActionListener(this);
        }
    }
    
    private void initProfiles(final J2MEPlatform.J2MEProfile profs[]) {
        Arrays.sort(profs);
        for (int i=0; i<profs.length; i++) {
            final JRadioButton btn = new JRadioButton(profs[i].toString()); // TO DO some text formating
            btn.setToolTipText(profs[i].getDisplayNameWithVersion());
            btn.setActionCommand(profs[i].toString());
            btn.setPreferredSize(new java.awt.Dimension(firstConfigWidth, btn.getPreferredSize().height));
            btn.setMinimumSize(new java.awt.Dimension(firstConfigWidth, btn.getPreferredSize().height));
            profGroup.add(btn);
            jPanelProfile.add(btn);
            btn.addActionListener(this);
        }
    }
    
    private void initOptional(final J2MEPlatform.J2MEProfile opts[]) {
        Arrays.sort(opts, OPTIONAL_API_COMPARATOR);
        for (int i=0; i<opts.length; i++) {
            final String dName = opts[i].isNameIsJarFileName() ? opts[i].getDisplayName() : opts[i].getDisplayNameWithVersion();
            final JCheckBox cb = new JCheckBox(dName);
            cb.setToolTipText(dName);
            cb.setActionCommand(opts[i].toString());
            optional.add(cb);
            cb.addActionListener(this);
        }
    }
    
    
    public void initValues(ProjectProperties props, String configuration) {
        this.props = props;
        this.vps = VisualPropertySupport.getDefault(props);
        this.configuration = configuration;
    }
    
    public void initGroupValues(final boolean useDefault) {
        jComboBoxTarget.removeActionListener(this);
        if (platformNames.length > 0) {
            vps.register(jComboBoxTarget, platformNames, DefaultPropertiesDescriptor.PLATFORM_ACTIVE, useDefault);
            this.useDefault = useDefault;
            jComboBoxTarget.addActionListener(this);
        } else {
            jComboBoxTarget.removeAllItems();
            final String errorMessage = NbBundle.getMessage(CustomizerMIDP.class, "ERR_CustMIDP_NoPlatform"); //NOI18N
            jComboBoxTarget.addItem(errorMessage);
            jComboBoxTarget.setSelectedItem(errorMessage);
            jComboBoxTarget.setEnabled(false);
        }
        initDevices((String)jComboBoxTarget.getSelectedItem(), false);
        enableLabels(!useDefault);
        props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.JAVAC_SOURCE, useDefault), "1.3"); //NOI18N
        props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.JAVAC_TARGET, useDefault), "1.3"); //NOI18N

    }
    
    private void enableLabels(final boolean enabled) {
        jLabelDevice.setEnabled(enabled);
        jLabelConfig.setEnabled(enabled);
        jLabelTarget.setEnabled(enabled);
        jLabelOptional.setEnabled(enabled);
        jLabelProfile.setEnabled(enabled);
        jButtonEdit.setEnabled(enabled);
    }
    
    public void actionPerformed(final ActionEvent e) {
        if (jComboBoxTarget.equals(e.getSource())) {
            initDevices((String)jComboBoxTarget.getSelectedItem(), true);
        } else if  (jComboDevice.equals(e.getSource())) {
            initAllProfiles((String)jComboDevice.getSelectedItem(), true);
        } else if (e.getSource() instanceof JCheckBox) {
            final JCheckBox cb = (JCheckBox) e.getSource();
            if (cb.isSelected()) detectCollisions(cb.getActionCommand());
            saveOptionalAPIs();
            saveClassPath();
        } else if (e.getSource() instanceof JRadioButton) {
            saveClassPath();
        }
    }
    
    private synchronized void initDevices(final String platformName, final boolean reset) {
        final J2MEPlatform platform = name2platform.get(platformName);
        if (platform != null) {
            props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION, useDefault), platform.getDisplayName());
            props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_TYPE, useDefault), platform.getType());
            final J2MEPlatform.Device[] devices = platform.getDevices();
            jComboDevice.removeActionListener(this);
            name2device = new HashMap<String,J2MEPlatform.Device>();
            for (int i=0; i<devices.length; i++) if (devices[i].isValid()) {
                name2device.put(devices[i].getName(), devices[i]);
            }
            String[] devNames = name2device.keySet().toArray(new String[name2device.size()]);
            Arrays.sort(devNames);
            vps.register(jComboDevice, devNames, DefaultPropertiesDescriptor.PLATFORM_DEVICE, useDefault);
            initAllProfiles((String)jComboDevice.getSelectedItem(), reset);
            jComboDevice.addActionListener(this);
        } else {
            jComboDevice.removeAllItems();
            jComboDevice.setEnabled(false);
            enableLabels(false);
        }
    }
    
    private synchronized void initAllProfiles(final String deviceName, final boolean reset) {
        final J2MEPlatform.Device device = name2device.get(deviceName);
        final HashSet<String> profNames = new HashSet<String>();
        String defaultCfg = null, defaultProf = null;
        final HashSet<String> defaultOpts = new HashSet<String>();
        name2profile = new HashMap<String,J2MEPlatform.J2MEProfile>();
        //collect all available configurations, profiles, and optional packages
        if (device != null) {
            final J2MEPlatform.J2MEProfile prof[] = device.getProfiles();
            for (int i=0; i<prof.length; i++) {
                profNames.add(prof[i].toString());
                name2profile.put(prof[i].toString(), prof[i]);
                if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(prof[i].getType()) && prof[i].isDefault()) {
                    defaultCfg = prof[i].toString();
                } else if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(prof[i].getType()) && prof[i].isDefault()) {
                    defaultProf = prof[i].toString();
                } else if (J2MEPlatform.J2MEProfile.TYPE_OPTIONAL.equals(prof[i].getType()) && prof[i].isDefault()) {
                    defaultOpts.add(prof[i].toString());
                }
            }
        }
        //enable/disable configuration radio boxes
        Component c[] = jPanelConfig.getComponents();
        for (int i=0; i<c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                final JRadioButton rb = (JRadioButton)c[i];
                vps.register(rb, DefaultPropertiesDescriptor.PLATFORM_CONFIGURATION, useDefault);
                if (profNames.contains(rb.getActionCommand())) {
                    rb.setEnabled(!useDefault);
                } else {
                    rb.setEnabled(false);
                }
            }
        }
        //correct selection of configuration to default if necessary
        if (reset && defaultCfg != null) {
            selectDefault(jPanelConfig, defaultCfg, DefaultPropertiesDescriptor.PLATFORM_CONFIGURATION);
        }
        //enable/disable profile radio boxes
        c = jPanelProfile.getComponents();
        for (int i=0; i<c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                final JRadioButton rb = (JRadioButton)c[i];
                vps.register(rb, DefaultPropertiesDescriptor.PLATFORM_PROFILE, useDefault);
                if (profNames.contains(rb.getActionCommand())) {
                    rb.setEnabled(!useDefault);
                } else {
                    rb.setEnabled(false);
                }
            }
        }
        //correct selection of profile to default if necessary
        if (reset && defaultCfg != null) {
            selectDefault(jPanelProfile, defaultProf, DefaultPropertiesDescriptor.PLATFORM_PROFILE);
        }
        //enable/disable optional package check boxes
        final Set<String> optValues = getOptionalValues();
        jPanelOptional.setVisible(false);
        jPanelOptional.removeAll();
        for (final JCheckBox cb : optional ) {
            final String APIname = cb.getActionCommand();
            final boolean selected = (reset ? defaultOpts : optValues).contains(APIname);
            if (profNames.contains(APIname)) {
                jPanelOptional.add(cb, new GridBagConstraints(0, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                cb.setEnabled(!useDefault);
                cb.setSelected(selected);
            }
        }
        jPanelOptional.add(new JPanel(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        jPanelOptional.setVisible(true);
        jPanelOptional.repaint();
        jPanelOptional.validate();
        saveOptionalAPIs();
        saveClassPath();
    }
        
    private Set<String> getOptionalValues() {
        final String s = (String)props.get(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_APIS, useDefault));
        if (s == null) return Collections.EMPTY_SET;
        final HashSet<String> vals = new HashSet<String>();
        final StringTokenizer stk = new StringTokenizer(s, ","); //NOI18N
        while (stk.hasMoreTokens()) {
            vals.add(stk.nextToken());
        }
        return vals;
    }
    
    private void selectDefault(final JPanel panel, final String name, final String property) {
        final Component c[] = panel.getComponents();
        for (int i=0; i<c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                final JRadioButton rb = (JRadioButton)c[i];
                if (name.equals(rb.getActionCommand())) {
                    props.put(VisualPropertySupport.translatePropertyName(configuration, property, useDefault), name);
                    vps.register(rb, property, useDefault);
                }
            }
        }
    }
    
    private void detectCollisions(final String apiName) {
        final J2MEPlatform.J2MEProfile newProfile = name2profile.get(apiName);
        if (newProfile == null) return;
        final Component c[] = jPanelOptional.getComponents();
        for (int i = 0 ; i < c.length - 1 ; i++) {
            final JCheckBox cb = (JCheckBox)c[i];
            if (cb.isSelected()) {
                final J2MEPlatform.J2MEProfile profile = name2profile.get(cb.getActionCommand());
                if (profile != null && !profile.equals(newProfile) && profile.getName().equals(newProfile.getName()) && cb.isEnabled()) {
                    cb.setSelected(false);
                }
            }
        }
    }
    
    private synchronized void saveOptionalAPIs() {
        final StringBuffer sb = new StringBuffer();
        final Component c[] = jPanelOptional.getComponents();
        for (int i = 0 ; i < c.length - 1 ; i++) {
            final JCheckBox cb = (JCheckBox)c[i];
            if (cb.isSelected()) {
                if (sb.length() > 0) sb.append(',');
                sb.append(cb.getActionCommand());
            }
        }
        final String propName = VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_APIS, useDefault);
        props.put(propName, sb.toString());
    }
    
    private synchronized void saveClassPath() {
        final StringBuffer classpath = new StringBuffer();
        Component c[] = jPanelConfig.getComponents();
        for (int i=0; i<c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                final JRadioButton rb = (JRadioButton)c[i];
                if (rb.isSelected()) {
                    final J2MEPlatform.J2MEProfile profile = name2profile.get(rb.getActionCommand());
                    if (profile != null) {
                        if (classpath.length() > 0) classpath.append(':');
                        classpath.append(profile.getClassPath());
                    }
                }
            }
        }
        c = jPanelProfile.getComponents();
        for (int i=0; i<c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                final JRadioButton rb = (JRadioButton)c[i];
                if (rb.isSelected()) {
                    final J2MEPlatform.J2MEProfile profile = name2profile.get(rb.getActionCommand());
                    if (profile != null) {
                        if (classpath.length() > 0) classpath.append(':');
                        classpath.append(profile.getClassPath());
                    }
                }
            }
        }
        c = jPanelOptional.getComponents();
        for (int i = 0 ; i < c.length - 1 ; i++) {
            final JCheckBox cb = (JCheckBox)c[i];
            if (cb.isSelected()) {
                final J2MEPlatform.J2MEProfile profile = name2profile.get(cb.getActionCommand());
                if (profile != null) {
                    if (classpath.length() > 0) classpath.append(':');
                    classpath.append(profile.getClassPath());
                }
            }
        }
        
        final J2MEPlatform.Device device = name2device.get(jComboDevice.getSelectedItem());
        
        props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_BOOTCLASSPATH, useDefault), device == null ? classpath.toString() : device.sortClasspath(classpath.toString()));
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
        jButtonEdit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelDevice = new javax.swing.JLabel();
        jComboDevice = new javax.swing.JComboBox();
        jLabelConfig = new javax.swing.JLabel();
        jPanelConfig = new javax.swing.JPanel();
        jLabelProfile = new javax.swing.JLabel();
        jPanelProfile = new javax.swing.JPanel();
        jLabelOptional = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelOptional = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabelTarget.setLabelFor(jComboBoxTarget);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTarget, NbBundle.getMessage(CustomizerMIDP.class, "LBL_CustMIDP_TargetPlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelTarget, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jComboBoxTarget, gridBagConstraints);
        jComboBoxTarget.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerMIDP.class, "ACSD_CustMIDP_Platform")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEdit, NbBundle.getMessage(CustomizerMIDP.class, "LBL_CustMIDP_Edit")); // NOI18N
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jButtonEdit, gridBagConstraints);
        jButtonEdit.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerMIDP.class, "ACSD_CustMIDP_Edit")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        jLabelDevice.setLabelFor(jComboDevice);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDevice, NbBundle.getMessage(CustomizerMIDP.class, "LBL_CustMIDP_Device")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelDevice, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(jComboDevice, gridBagConstraints);
        jComboDevice.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerMIDP.class, "ACSD_CustMIDP_Device")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelConfig, NbBundle.getMessage(CustomizerMIDP.class, "LBL_CustMIDP_Configuration")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelConfig, gridBagConstraints);

        jPanelConfig.setLayout(new java.awt.GridLayout(1, 0, 5, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jPanelConfig, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelProfile, NbBundle.getMessage(CustomizerMIDP.class, "LBL_CustMIDP_Profile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelProfile, gridBagConstraints);

        jPanelProfile.setLayout(new java.awt.GridLayout(1, 0, 5, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(jPanelProfile, gridBagConstraints);

        jLabelOptional.setLabelFor(jPanelOptional);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelOptional, NbBundle.getMessage(CustomizerMIDP.class, "LBL_CustMIDP_Optional")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelOptional, gridBagConstraints);

        jPanelOptional.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jPanelOptional);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerMIDP.class, "ACSN_CustMIDP"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerMIDP.class, "ACSD_CustMIDP"));
    }
    
    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        //System.out.println(name2platform.get(jComboBoxTarget.getSelectedItem()));
        PlatformsCustomizer.showCustomizer(name2platform.get(jComboBoxTarget.getSelectedItem()));
        refreshAll();
    }//GEN-LAST:event_jButtonEditActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup cfgGroup;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JComboBox jComboDevice;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JLabel jLabelDevice;
    private javax.swing.JLabel jLabelOptional;
    private javax.swing.JLabel jLabelProfile;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JPanel jPanelOptional;
    private javax.swing.JPanel jPanelProfile;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.ButtonGroup profGroup;
    // End of variables declaration//GEN-END:variables
    
    private int firstConfigWidth = -1;
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    // Private methods for classpath data manipulation -------------------------
    
}
