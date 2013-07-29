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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.profiler.stp;

import java.awt.BorderLayout;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.ui.UIUtils;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "CPUSettingsAdvancedPanel_SchemeComboBoxItemLazy=Lazy",
    "CPUSettingsAdvancedPanel_SchemeComboBoxItemEager=Eager",
    "CPUSettingsAdvancedPanel_SchemeComboBoxItemTotal=Total",
    "CPUSettingsAdvancedPanel_DoNotOverrideString=<Do not override>",
    "CPUSettingsAdvancedPanel_ChooseWorkDirDialogCaption=Choose Working Directory",
    "CPUSettingsAdvancedPanel_SettingsCaption=Settings",
    "CPUSettingsAdvancedPanel_MethodsTrackingLabelText=Methods tracking:",
    "CPUSettingsAdvancedPanel_InstrRadioText=&Exact call tree and timing",
    "CPUSettingsAdvancedPanel_SamplingRadioText=Exact call tree, &sampled timing",
    "CPUSettingsAdvancedPanel_ExcludeTimeCheckboxText=E&xclude time spent in Thread.sleep() and Object.wait()",
    "CPUSettingsAdvancedPanel_ProfileFrameworkCheckboxText=&Profile underlying framework startup",
    "CPUSettingsAdvancedPanel_ProfileThreadsCheckboxText=Profile new &Threads/Runnables",
    "CPUSettingsAdvancedPanel_LimitThreadsCheckboxText=&Limit number of profiled threads:",
    "CPUSettingsAdvancedPanel_ThreadTimerCheckboxText=&Use thread CPU timer",
    "CPUSettingsAdvancedPanel_InstrSchemeLabelText=&Instrumentation scheme:",
    "CPUSettingsAdvancedPanel_InstrumentLabelText=Instrument:",
    "CPUSettingsAdvancedPanel_MethodInvokeCheckboxText=Method.in&voke()",
    "CPUSettingsAdvancedPanel_GetterSetterCheckboxText=&Getter/setter methods",
    "CPUSettingsAdvancedPanel_EmptyMethodsCheckboxText=Empt&y methods",
    "CPUSettingsAdvancedPanel_ThreadsCaption=Threads",
    "CPUSettingsAdvancedPanel_EnableThreadsCheckboxText=E&nable threads monitoring",
    "CPUSettingsAdvancedPanel_EnableSamplingCheckboxText=&Sample threads states",
    "CPUSettingsAdvancedPanel_EnableLockContentionCheckboxText=En&able lock contention monitoring"
})
public class CPUSettingsAdvancedPanel extends DefaultSettingsPanel implements HelpCtx.Provider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final String HELP_CTX_KEY = "CPUSettings.Advanced.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JCheckBox excludeTimeCheckbox;
    private JCheckBox instrumentEmptyMethodsCheckbox;
    private JCheckBox instrumentGettersSettersCheckbox;
    private JCheckBox instrumentMethodInvokeCheckbox;
    private JCheckBox limitThreadsCheckbox;
    private JCheckBox profileFrameworkCheckbox;
    private JCheckBox profileSpawnedThreadsCheckbox;
    private JCheckBox threadsMonitoringCheckbox;
    private JCheckBox threadsSamplingCheckbox;
    private JCheckBox lockContentionMonitoringCheckbox;
    private JCheckBox useCPUTimerCheckbox;
    private JComboBox instrumentationSchemeCombo;
    private JLabel instrumentLabel;
    private JLabel instrumentationSchemeLabel;
    private JLabel methodsTrackingLabel;
    private JLabel sampledTimingLabel;
    private JLabel samplingFrequencyLabel;
    private JLabel samplingFrequencyUnitsLabel;

    // --- UI components declaration ---------------------------------------------
    private JPanel settingsPanel;
    private JPanel samplSettingsPanel;
    private JPanel instrSettingsPanel;
    private JPanel threadsSettingsPanel;
    private JRadioButton exactTimingRadio;
    private JRadioButton sampledTimingRadio;
    private JSpinner limitThreadsSpinner;
    private JSpinner sampledTimingSpinner;
    private JSpinner samplingFrequencySpinner;
    
    private int profilingType;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public CPUSettingsAdvancedPanel() {
        super();
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    public void setProfilingType(int profilingType) {
        this.profilingType = profilingType;
        if (profilingType == ProfilingSettings.PROFILE_CPU_SAMPLING) {
            settingsPanel.removeAll();
            settingsPanel.add(samplSettingsPanel);
        } else {
            settingsPanel.removeAll();
            settingsPanel.add(instrSettingsPanel);
        }
    }

    public void setCPUProfilingType(int type) { // CommonConstants.INSTR_RECURSIVE_FULL or SAMPLED
        if (profilingType == ProfilingSettings.PROFILE_CPU_SAMPLING) {
            return;
        }
        exactTimingRadio.setSelected(type == CommonConstants.CPU_INSTR_FULL);
        sampledTimingRadio.setSelected(type == CommonConstants.CPU_INSTR_SAMPLED);
        sampledTimingSpinner.setEnabled(sampledTimingRadio.isSelected());
    }

    public int getCPUProfilingType() {
        if (profilingType == ProfilingSettings.PROFILE_CPU_SAMPLING) {
            return CommonConstants.CPU_SAMPLED;
        }
        if (exactTimingRadio.isSelected()) {
            return CommonConstants.CPU_INSTR_FULL;
        } else {
            return CommonConstants.CPU_INSTR_SAMPLED;
        }
    }
    
    public void setSamplingFrequency(int samplingFrequency) {
        samplingFrequencySpinner.setValue(Integer.valueOf(samplingFrequency));
    }
    
    public int getSamplingFrequency() {
        return ((Integer) samplingFrequencySpinner.getValue()).intValue();
    }

    public void setEntireAppDefaults(boolean isPreset) {
        if (isPreset) {
            profileSpawnedThreadsCheckbox.setSelected(false);
        }

        if (isPreset) {
            instrumentationSchemeCombo.setSelectedItem(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemTotal());
        }

        if (isPreset) {
            profileFrameworkCheckbox.setSelected(false);
        }

        if (!isPreset) {
            profileFrameworkCheckbox.setEnabled(true);
        }
        
        if (isPreset) {
            useCPUTimerCheckbox.setSelected(false);
        }
        
        if (isPreset) {
            exactTimingRadio.setSelected(true);
        }
    }

    public void setExcludeThreadTime(boolean exclude) {
        excludeTimeCheckbox.setSelected(exclude);
    }

    public boolean getExcludeThreadTime() {
        return excludeTimeCheckbox.isSelected();
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public void setInstrumentEmptyMethods(boolean instrument) {
        instrumentEmptyMethodsCheckbox.setSelected(instrument);
    }

    public boolean getInstrumentEmptyMethods() {
        return instrumentEmptyMethodsCheckbox.isSelected();
    }

    public void setInstrumentGettersSetters(boolean instrument) {
        instrumentGettersSettersCheckbox.setSelected(instrument);
    }

    public boolean getInstrumentGettersSetters() {
        return instrumentGettersSettersCheckbox.isSelected();
    }

    public void setInstrumentMethodInvoke(boolean instrument) {
        instrumentMethodInvokeCheckbox.setSelected(instrument);
    }

    public boolean getInstrumentMethodInvoke() {
        return instrumentMethodInvokeCheckbox.isSelected();
    }

    public void setInstrumentationScheme(int scheme) {
        if (scheme == CommonConstants.INSTRSCHEME_LAZY) {
            instrumentationSchemeCombo.setSelectedItem(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemLazy());
        } else if (scheme == CommonConstants.INSTRSCHEME_EAGER) {
            instrumentationSchemeCombo.setSelectedItem(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemEager());
        } else {
            instrumentationSchemeCombo.setSelectedItem(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemTotal());
        }
    }

    public int getInstrumentationScheme() {
        Object selectedScheme = instrumentationSchemeCombo.getSelectedItem();

        if (selectedScheme.equals(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemLazy())) {
            return CommonConstants.INSTRSCHEME_LAZY;
        } else if (selectedScheme.equals(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemEager())) {
            return CommonConstants.INSTRSCHEME_EAGER;
        } else {
            return CommonConstants.INSTRSCHEME_TOTAL;
        }
    }

    public void setPartOfAppDefaults(boolean isPreset) {
        if (isPreset) {
            profileSpawnedThreadsCheckbox.setSelected(false);
        }

        if (isPreset) {
            instrumentationSchemeCombo.setSelectedItem(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemLazy());
        }
        
        if (isPreset) {
            useCPUTimerCheckbox.setSelected(false);
        }

        profileFrameworkCheckbox.setSelected(false);
        profileFrameworkCheckbox.setEnabled(false);
    }

    public void setProfileFramework(boolean profile) {
        profileFrameworkCheckbox.setSelected(profile);
    }

    public boolean getProfileFramework() {
        return profileFrameworkCheckbox.isSelected();
    }

    public void setProfileSpawnedThreads(boolean profile) {
        profileSpawnedThreadsCheckbox.setSelected(profile);
    }

    public boolean getProfileSpawnedThreads() {
        return profileSpawnedThreadsCheckbox.isSelected();
    }

    public void setProfiledThreadsLimit(int limit) {
        limitThreadsCheckbox.setSelected(limit > 0);
        limitThreadsSpinner.setValue(Math.abs(Integer.valueOf(limit)));
        limitThreadsSpinner.setEnabled(limitThreadsCheckbox.isSelected());
    }

    public int getProfiledThreadsLimit() {
        if (limitThreadsCheckbox.isSelected()) {
            return ((Integer) limitThreadsSpinner.getValue()).intValue();
        } else {
            return -((Integer) limitThreadsSpinner.getValue()).intValue();
        }
    }

    public void setSamplingInterval(int samplingInterval) {
        sampledTimingSpinner.setValue(Integer.valueOf(samplingInterval));
    }

    public int getSamplingInterval() {
        return ((Integer) sampledTimingSpinner.getValue()).intValue();
    }

    public void setThreadsMonitoring(boolean enabled) {
        threadsMonitoringCheckbox.setSelected(enabled);
    }

    public boolean getThreadsMonitoring() {
        return threadsMonitoringCheckbox.isSelected();
    }

    public void setThreadsSampling(boolean enabled) {
        threadsSamplingCheckbox.setSelected(enabled);
    }

    public boolean getThreadsSampling() {
        return threadsSamplingCheckbox.isSelected();
    }
    
    public void setLockContentionMonitoring(boolean enabled) {
        lockContentionMonitoringCheckbox.setSelected(enabled);
    }

    public boolean getLockContentionMonitoring() {
        return lockContentionMonitoringCheckbox.isSelected();
    }

    public void setUseCPUTimer(boolean use, boolean available) {
        useCPUTimerCheckbox.setSelected(use);
        useCPUTimerCheckbox.setEnabled(available);
    }

    public boolean getUseCPUTimer() {
        return profilingType == ProfilingSettings.PROFILE_CPU_SAMPLING ||
               useCPUTimerCheckbox.isSelected();
    }

    public void disableAll() {
        methodsTrackingLabel.setEnabled(false);
        exactTimingRadio.setEnabled(false);
        sampledTimingRadio.setEnabled(false);
        sampledTimingSpinner.setEnabled(false);
        sampledTimingLabel.setEnabled(false);
        excludeTimeCheckbox.setEnabled(false);
        profileFrameworkCheckbox.setEnabled(false);
        profileSpawnedThreadsCheckbox.setEnabled(false);
        limitThreadsCheckbox.setEnabled(false);
        limitThreadsSpinner.setEnabled(false);
        useCPUTimerCheckbox.setEnabled(false);
        instrumentationSchemeLabel.setEnabled(false);
        instrumentationSchemeCombo.setEnabled(false);
        instrumentLabel.setEnabled(false);
        instrumentMethodInvokeCheckbox.setEnabled(false);
        instrumentGettersSettersCheckbox.setEnabled(false);
        instrumentEmptyMethodsCheckbox.setEnabled(false);
        samplingFrequencyLabel.setEnabled(false);
        samplingFrequencyUnitsLabel.setEnabled(false);
        samplingFrequencySpinner.setEnabled(false);

        threadsSettingsPanel.setEnabled(false);
        threadsMonitoringCheckbox.setEnabled(false);
        threadsSamplingCheckbox.setEnabled(false);
        lockContentionMonitoringCheckbox.setEnabled(false);
    }

    public void enableAll() {
        methodsTrackingLabel.setEnabled(true);
        exactTimingRadio.setEnabled(true);
        sampledTimingRadio.setEnabled(true);
        sampledTimingSpinner.setEnabled(true);
        sampledTimingLabel.setEnabled(true);
        excludeTimeCheckbox.setEnabled(true);
        profileFrameworkCheckbox.setEnabled(true);
        profileSpawnedThreadsCheckbox.setEnabled(true);
        limitThreadsCheckbox.setEnabled(true);
        limitThreadsSpinner.setEnabled(true);
        useCPUTimerCheckbox.setEnabled(true);
        instrumentationSchemeLabel.setEnabled(true);
        instrumentationSchemeCombo.setEnabled(true);
        instrumentLabel.setEnabled(true);
        instrumentMethodInvokeCheckbox.setEnabled(true);
        instrumentGettersSettersCheckbox.setEnabled(true);
        instrumentEmptyMethodsCheckbox.setEnabled(true);
        samplingFrequencyLabel.setEnabled(true);
        samplingFrequencyUnitsLabel.setEnabled(true);
        samplingFrequencySpinner.setEnabled(true);

        threadsSettingsPanel.setEnabled(true);
        threadsMonitoringCheckbox.setEnabled(true);
        threadsSamplingCheckbox.setEnabled(true);
        lockContentionMonitoringCheckbox.setEnabled(true);

    }

    // --- Static tester frame ---------------------------------------------------

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("plaf.metal.MetalLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //NOI18N
        } catch (Exception e) {
        }

        ;

        JFrame frame = new JFrame("Tester Frame"); //NOI18N
        JPanel contents = new CPUSettingsAdvancedPanel();
        contents.setPreferredSize(new Dimension(375, 255));
        frame.getContentPane().add(contents);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    // --- UI definition ---------------------------------------------------------
    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        ButtonGroup methodsTrackingRadiosGroup = new ButtonGroup();

        // settingsPanel
        settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 5, 10, 5);
        add(settingsPanel, constraints);
        
        // samplSettingsPanel
        samplSettingsPanel = new JPanel(new GridBagLayout());
        samplSettingsPanel.setOpaque(false);
        samplSettingsPanel.setBorder(BorderFactory.createTitledBorder(Bundle.CPUSettingsAdvancedPanel_SettingsCaption()));
//        settingsPanel.add(samplSettingsPanel, "SAMPL");
        
        // samplingFrequencyContainer - definition
        JPanel samplingFrequencyContainer = new JPanel(new GridBagLayout());

        // samplingFrequencyLabel
        samplingFrequencyLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(samplingFrequencyLabel, Bundle.StpSamplingFrequencyLabel());
        samplingFrequencyLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 5);
        samplingFrequencyContainer.add(samplingFrequencyLabel, constraints);

        // samplingFrequencySpinner
        samplingFrequencySpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        samplingFrequencySpinner.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        samplingFrequencySpinner.addChangeListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        samplingFrequencyContainer.add(samplingFrequencySpinner, constraints);

        // sampledTimingLabel
        samplingFrequencyUnitsLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(samplingFrequencyUnitsLabel, Bundle.StpSamplingFrequencyMs());
        samplingFrequencyUnitsLabel.setLabelFor(samplingFrequencySpinner);
        samplingFrequencyUnitsLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        samplingFrequencyUnitsLabel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 5, 0, 0);
        samplingFrequencyContainer.add(samplingFrequencyUnitsLabel, constraints);

        // sampledTimingContainer - customization
        samplingFrequencyContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 7, 8, 0);
        samplSettingsPanel.add(samplingFrequencyContainer, constraints);
        
        // fillerPanel
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        samplSettingsPanel.add(UIUtils.createFillerPanel(), constraints);
        
        // instrSettingsPanel
        instrSettingsPanel = new JPanel(new GridBagLayout());
        instrSettingsPanel.setOpaque(false);
        instrSettingsPanel.setBorder(BorderFactory.createTitledBorder(Bundle.CPUSettingsAdvancedPanel_SettingsCaption()));

        // methodsTrackingLabel
        methodsTrackingLabel = new JLabel(Bundle.CPUSettingsAdvancedPanel_MethodsTrackingLabelText());
        methodsTrackingLabel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 7, 0, 0);
        instrSettingsPanel.add(methodsTrackingLabel, constraints);

        // exactTimingRadio
        exactTimingRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(exactTimingRadio, Bundle.CPUSettingsAdvancedPanel_InstrRadioText());
        exactTimingRadio.setToolTipText(Bundle.StpExactTimingTooltip());
        methodsTrackingRadiosGroup.add(exactTimingRadio);
        exactTimingRadio.addActionListener(getSettingsChangeListener());
        exactTimingRadio.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 19, 0, 0);
        instrSettingsPanel.add(exactTimingRadio, constraints);

        // sampledTimingContainer - definition
        JPanel sampledTimingContainer = new JPanel(new GridBagLayout());

        // sampledTimingRadio
        sampledTimingRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(sampledTimingRadio, Bundle.CPUSettingsAdvancedPanel_SamplingRadioText());
        sampledTimingRadio.setToolTipText(Bundle.StpSampledTimingTooltip());
        methodsTrackingRadiosGroup.add(sampledTimingRadio);
        sampledTimingRadio.setOpaque(false);
        sampledTimingRadio.setSelected(true);
        sampledTimingRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    sampledTimingSpinner.setEnabled(sampledTimingRadio.isSelected());
                    sampledTimingLabel.setEnabled(sampledTimingRadio.isSelected());
                }
            });
        sampledTimingRadio.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 5);
        sampledTimingContainer.add(sampledTimingRadio, constraints);

        // sampledTimingSpinner
        sampledTimingSpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        sampledTimingSpinner.setToolTipText(Bundle.StpSampledTimingTooltip());
        sampledTimingSpinner.addChangeListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        sampledTimingContainer.add(sampledTimingSpinner, constraints);

        // sampledTimingLabel
        sampledTimingLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(sampledTimingLabel, Bundle.StpSamplingFrequencyMs());
        sampledTimingLabel.setLabelFor(sampledTimingSpinner);
        sampledTimingLabel.setToolTipText(Bundle.StpSampledTimingTooltip());
        sampledTimingLabel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 5, 0, 0);
        sampledTimingContainer.add(sampledTimingLabel, constraints);

        // sampledTimingContainer - customization
        sampledTimingContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 19, 5, 0);
        instrSettingsPanel.add(sampledTimingContainer, constraints);

        // excludeTimeCheckbox
        excludeTimeCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(excludeTimeCheckbox, Bundle.CPUSettingsAdvancedPanel_ExcludeTimeCheckboxText());
        excludeTimeCheckbox.setToolTipText(Bundle.StpSleepWaitTooltip());
        excludeTimeCheckbox.addActionListener(getSettingsChangeListener());
        excludeTimeCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(excludeTimeCheckbox, constraints);

        // profileFrameworkCheckbox
        profileFrameworkCheckbox = new JCheckBox() {
            public void setSelected(boolean b) {
                super.setSelected(b);
                updateEnabling();
            }
        };
        org.openide.awt.Mnemonics.setLocalizedText(profileFrameworkCheckbox, Bundle.CPUSettingsAdvancedPanel_ProfileFrameworkCheckboxText());
        profileFrameworkCheckbox.setToolTipText(Bundle.StpFrameworkTooltip());
        profileFrameworkCheckbox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateEnabling();
                }
            });
        profileFrameworkCheckbox.addActionListener(getSettingsChangeListener());
        profileFrameworkCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(profileFrameworkCheckbox, constraints);

        // profileSpawnedThreadsCheckbox
        profileSpawnedThreadsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(profileSpawnedThreadsCheckbox, Bundle.CPUSettingsAdvancedPanel_ProfileThreadsCheckboxText());
        profileSpawnedThreadsCheckbox.setToolTipText(Bundle.StpSpawnedTooltip());
        profileSpawnedThreadsCheckbox.addActionListener(getSettingsChangeListener());
        profileSpawnedThreadsCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(profileSpawnedThreadsCheckbox, constraints);

        // limitThreadsContainer - definition
        JPanel limitThreadsContainer = new JPanel(new GridBagLayout());

        // limitThreadsCheckbox
        limitThreadsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(limitThreadsCheckbox, Bundle.CPUSettingsAdvancedPanel_LimitThreadsCheckboxText());
        limitThreadsCheckbox.setToolTipText(Bundle.StpLimitThreadsTooltip());
        limitThreadsCheckbox.addActionListener(getSettingsChangeListener());
        limitThreadsCheckbox.setOpaque(false);
        limitThreadsCheckbox.setSelected(true);
        limitThreadsCheckbox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    limitThreadsSpinner.setEnabled(limitThreadsCheckbox.isSelected());
                }
            });
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 5);
        limitThreadsContainer.add(limitThreadsCheckbox, constraints);

        // limitThreadsSpinner
        limitThreadsSpinner = new JExtendedSpinner(new SpinnerNumberModel(50, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        limitThreadsSpinner.setToolTipText(Bundle.StpLimitThreadsTooltip());
        limitThreadsSpinner.addChangeListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        limitThreadsContainer.add(limitThreadsSpinner, constraints);

        // limitThreadsContainer - customization
        limitThreadsContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(limitThreadsContainer, constraints);

        // useCPUTimerCheckbox
        useCPUTimerCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(useCPUTimerCheckbox, Bundle.CPUSettingsAdvancedPanel_ThreadTimerCheckboxText());
        useCPUTimerCheckbox.setToolTipText(Bundle.StpCpuTimerTooltip());
        useCPUTimerCheckbox.addActionListener(getSettingsChangeListener());
        useCPUTimerCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(useCPUTimerCheckbox, constraints);

        // instrumentationSchemeContainer - definition
        JPanel instrumentationSchemeContainer = new JPanel(new GridBagLayout());

        // instrumentationSchemeLabel
        instrumentationSchemeLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentationSchemeLabel, Bundle.CPUSettingsAdvancedPanel_InstrSchemeLabelText());
        instrumentationSchemeLabel.setToolTipText(Bundle.StpInstrSchemeTooltip());
        instrumentationSchemeLabel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 5);
        instrumentationSchemeContainer.add(instrumentationSchemeLabel, constraints);

        // instrumentationSchemeCombo
        instrumentationSchemeCombo = new JComboBox(new String[] {
                                                       Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemLazy(), 
                                                       Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemEager(),
                                                       Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemTotal()
                                                   }) {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        instrumentationSchemeLabel.setLabelFor(instrumentationSchemeCombo);
        instrumentationSchemeCombo.setToolTipText(Bundle.StpInstrSchemeTooltip());
        instrumentationSchemeCombo.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        instrumentationSchemeContainer.add(instrumentationSchemeCombo, constraints);

        // instrumentationSchemeContainer - customization
        instrumentationSchemeContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 8;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 7, 2, 0);
        instrSettingsPanel.add(instrumentationSchemeContainer, constraints);

        // instrumentLabel
        instrumentLabel = new JLabel(Bundle.CPUSettingsAdvancedPanel_InstrumentLabelText());
        instrumentLabel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 9;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(instrumentLabel, constraints);

        // instrumentMethodInvokeCheckbox
        instrumentMethodInvokeCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentMethodInvokeCheckbox, Bundle.CPUSettingsAdvancedPanel_MethodInvokeCheckboxText());
        instrumentMethodInvokeCheckbox.setToolTipText(Bundle.StpMethodInvokeTooltip());
        instrumentMethodInvokeCheckbox.addActionListener(getSettingsChangeListener());
        instrumentMethodInvokeCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 10;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 19, 0, 0);
        instrSettingsPanel.add(instrumentMethodInvokeCheckbox, constraints);

        // instrumentGettersSettersCheckbox
        instrumentGettersSettersCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentGettersSettersCheckbox, Bundle.CPUSettingsAdvancedPanel_GetterSetterCheckboxText());
        instrumentGettersSettersCheckbox.setToolTipText(Bundle.StpGetterSetterTooltip());
        instrumentGettersSettersCheckbox.addActionListener(getSettingsChangeListener());
        instrumentGettersSettersCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 11;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 19, 0, 0);
        instrSettingsPanel.add(instrumentGettersSettersCheckbox, constraints);

        // instrumentEmptyMethodsCheckbox
        instrumentEmptyMethodsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentEmptyMethodsCheckbox, Bundle.CPUSettingsAdvancedPanel_EmptyMethodsCheckboxText());
        instrumentEmptyMethodsCheckbox.setToolTipText(Bundle.StpEmptyMethodsTooltip());
        instrumentEmptyMethodsCheckbox.addActionListener(getSettingsChangeListener());
        instrumentEmptyMethodsCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 12;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 19, 3, 0);
        instrSettingsPanel.add(instrumentEmptyMethodsCheckbox, constraints);

        // threadsSettingsPanel
        threadsSettingsPanel = new JPanel(new GridBagLayout());
        threadsSettingsPanel.setOpaque(false);
        threadsSettingsPanel.setBorder(BorderFactory.createTitledBorder(Bundle.CPUSettingsAdvancedPanel_ThreadsCaption()));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 5, 10, 5);
        add(threadsSettingsPanel, constraints);

        // threadsMonitoringCheckbox
        threadsMonitoringCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(threadsMonitoringCheckbox, Bundle.CPUSettingsAdvancedPanel_EnableThreadsCheckboxText());
        threadsMonitoringCheckbox.setToolTipText(Bundle.StpMonitorTooltip());
        threadsMonitoringCheckbox.addActionListener(getSettingsChangeListener());
        threadsMonitoringCheckbox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateEnabling();
                }
            });
        threadsMonitoringCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 7, 1, 0);
        threadsSettingsPanel.add(threadsMonitoringCheckbox, constraints);

        // threadsSamplingCheckbox
        threadsSamplingCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(threadsSamplingCheckbox, Bundle.CPUSettingsAdvancedPanel_EnableSamplingCheckboxText());
        threadsSamplingCheckbox.setToolTipText(Bundle.StpSamplingTooltip());
        threadsSamplingCheckbox.addActionListener(getSettingsChangeListener());
        threadsSamplingCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 7, 1, 0);
        threadsSettingsPanel.add(threadsSamplingCheckbox, constraints);
        
        // lockContentionMonitoringCheckbox
        lockContentionMonitoringCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(lockContentionMonitoringCheckbox, Bundle.CPUSettingsAdvancedPanel_EnableLockContentionCheckboxText());
        lockContentionMonitoringCheckbox.setToolTipText(Bundle.StpLockContentionTooltip());
        lockContentionMonitoringCheckbox.addActionListener(getSettingsChangeListener());
        lockContentionMonitoringCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 7, 3, 0);
        threadsSettingsPanel.add(lockContentionMonitoringCheckbox, constraints);

        // fillerPanel
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(UIUtils.createFillerPanel(), constraints);
    }

    private void updateEnabling() {
        if (profileFrameworkCheckbox.isSelected()) {
            profileSpawnedThreadsCheckbox.setSelected(true);
            profileSpawnedThreadsCheckbox.setEnabled(false);
            instrumentationSchemeCombo.setSelectedItem(Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemTotal());
            instrumentationSchemeCombo.setEnabled(false);
        } else {
            profileSpawnedThreadsCheckbox.setEnabled(methodsTrackingLabel.isEnabled()); // Just a hack to detect settings for preset (always disabled)
            instrumentationSchemeCombo.setEnabled(methodsTrackingLabel.isEnabled()); // Just a hack to detect settings for preset (always disabled)
        }
        threadsSamplingCheckbox.setEnabled(threadsMonitoringCheckbox.isSelected() && methodsTrackingLabel.isEnabled());
    }
}
