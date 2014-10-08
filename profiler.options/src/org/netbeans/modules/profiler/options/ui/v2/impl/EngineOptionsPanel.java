/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.profiler.options.ui.v2.impl;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import static org.netbeans.lib.profiler.ui.components.JExtendedSpinner.getDefaultSpinnerHeight;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.options.ui.v2.ProfilerOptionsPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider( service = ProfilerOptionsPanel.class, position = 30 )
public final class EngineOptionsPanel extends ProfilerOptionsPanel {
    
    public EngineOptionsPanel() {
        initUI();
    }
    
    public String getDisplayName() {
        return "Engine";
    }

    public void storeTo(ProfilerIDESettings settings) {
    }

    public void loadFrom(ProfilerIDESettings settings) {
    }

    public boolean equalsTo(ProfilerIDESettings settings) {
        return true;
    }
    
    
    @NbBundle.Messages({
        "StpSamplingFrequencyLabel=Sampling frequency:",
        "StpSamplingFrequencyTooltip=Customize sampling frequency of the profiler.",
        "StpSamplingFrequencyMs=&ms",
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
        "CPUSettingsAdvancedPanel_EnableLockContentionCheckboxText=En&able lock contention monitoring",
        "MemorySettingsBasicPanel_TrackEveryLabelText=&Track every",
    //# Used as Track every [JSpinner] object allocations
        "MemorySettingsBasicPanel_AllocLabelText=object allocations",
        "MemorySettingsAdvancedPanel_DoNotOverrideString=<Do not override>",
        "MemorySettingsAdvancedPanel_ChooseWorkDirDialogCaption=Choose Working Directory",
        "MemorySettingsAdvancedPanel_SettingsCaption=Settings",
        "MemorySettingsAdvancedPanel_RecordTracesLabelText=Record stack trace for allocations:",
        "MemorySettingsAdvancedPanel_FullStackRadioText=&Full stack depth",
        "MemorySettingsAdvancedPanel_LimitStackRadioText=&Limit stack to",
    //# Used as Limit stack to [JSpinner] frames
        "MemorySettingsAdvancedPanel_FramesLabelText=fra&mes",
        "MemorySettingsAdvancedPanel_RunGcCheckboxText=&Run garbage collection when getting memory results",
        "MemorySettingsAdvancedPanel_ThreadsCaption=Threads",
        "MemorySettingsAdvancedPanel_EnableThreadsCheckboxText=E&nable threads monitoring",
        "MemorySettingsAdvancedPanel_EnableSamplingCheckboxText=&Sample threads states",
        "MemorySettingsAdvancedPanel_GlobalSettingsCaption=Global Settings",
        "MemorySettingsAdvancedPanel_OverrideSettingsCheckboxText=&Override global settings",
        "MemorySettingsAdvancedPanel_WorkDirLabelText=&Working directory:",
        "MemorySettingsAdvancedPanel_ChooseWorkDirLinkText=Choose...",
        "MemorySettingsAdvancedPanel_JavaPlatformLabelText=&Java platform:",
        "MemorySettingsAdvancedPanel_JvmArgumentsLabelText=JVM &arguments:"
    })
    private void initUI() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints c;
        int y = 0;
        int htab = 8;
        int hgap = 10;
        int vgap = 5;
        
        Separator dataTransferSeparator = new Separator("Threads Settings");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vgap * 2, 0);
        add(dataTransferSeparator, c);
        
        JCheckBox sampledThreadsChoice = new JCheckBox("Sample threads states");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, htab, vgap, 0);
        add(sampledThreadsChoice, c);
        
        Separator cpuSettingsSeparator = new Separator("CPU Settings");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(cpuSettingsSeparator, c);
        
        ButtonGroup methodsTrackingRadiosGroup = new ButtonGroup();

//        // settingsPanel
//        cpuSettingsPanel = new JPanel(new BorderLayout());
//        cpuSettingsPanel.setOpaque(false);
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = GridBagConstraints.REMAINDER;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.insets = new Insets(0, 5, 10, 5);
//        add(cpuSettingsPanel, c);
        
//        // samplSettingsPanel
//        samplSettingsPanel = new JPanel(new GridBagLayout());
//        samplSettingsPanel.setOpaque(false);
////        samplSettingsPanel.setBorder(BorderFactory.createTitledBorder(Bundle.CPUSettingsAdvancedPanel_SettingsCaption()));
////        settingsPanel.add(samplSettingsPanel, "SAMPL");
        
//        // samplingFrequencyContainer - definition
//        JPanel samplingFrequencyContainer = new JPanel(new GridBagLayout());
//
//        // samplingFrequencyLabel
//        samplingFrequencyLabel = new JLabel();
//        org.openide.awt.Mnemonics.setLocalizedText(samplingFrequencyLabel, Bundle.StpSamplingFrequencyLabel());
////        samplingFrequencyLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.fill = GridBagConstraints.NONE;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(0, 0, 0, 5);
//        samplingFrequencyContainer.add(samplingFrequencyLabel, c);
//
//        // samplingFrequencySpinner
//        samplingFrequencySpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1)) {
//                public Dimension getPreferredSize() {
//                    return new Dimension(55, getDefaultSpinnerHeight());
//                }
//
//                public Dimension getMinimumSize() {
//                    return getPreferredSize();
//                }
//            };
////        samplingFrequencySpinner.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
////        samplingFrequencySpinner.addChangeListener(getSettingsChangeListener());
//        c = new GridBagConstraints();
//        c.gridx = 1;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.fill = GridBagConstraints.NONE;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(0, 0, 0, 0);
//        samplingFrequencyContainer.add(samplingFrequencySpinner, c);
//
//        // sampledTimingLabel
//        samplingFrequencyUnitsLabel = new JLabel();
//        org.openide.awt.Mnemonics.setLocalizedText(samplingFrequencyUnitsLabel, Bundle.StpSamplingFrequencyMs());
//        samplingFrequencyUnitsLabel.setLabelFor(samplingFrequencySpinner);
////        samplingFrequencyUnitsLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
//        samplingFrequencyUnitsLabel.setOpaque(false);
//        c = new GridBagConstraints();
//        c.gridx = 2;
//        c.gridy = 0;
//        c.weightx = 1;
//        c.gridwidth = 1;
//        c.fill = GridBagConstraints.NONE;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(0, 5, 0, 0);
//        samplingFrequencyContainer.add(samplingFrequencyUnitsLabel, c);
//
//        // sampledTimingContainer - customization
//        samplingFrequencyContainer.setOpaque(false);
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 2;
//        c.gridwidth = GridBagConstraints.REMAINDER;
//        c.fill = GridBagConstraints.NONE;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(2, 7, 8, 0);
//        samplSettingsPanel.add(samplingFrequencyContainer, c);
//        
//        // fillerPanel
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 3;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.gridwidth = GridBagConstraints.REMAINDER;
//        c.fill = GridBagConstraints.BOTH;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.insets = new Insets(0, 0, 0, 0);
//        samplSettingsPanel.add(UIUtils.createFillerPanel(), c);
        
        // instrSettingsPanel
        instrSettingsPanel = new JPanel(new GridBagLayout());
        instrSettingsPanel.setOpaque(false);
//        instrSettingsPanel.setBorder(BorderFactory.createTitledBorder(Bundle.CPUSettingsAdvancedPanel_SettingsCaption()));
        
        JLabel methodsSamplingLabel = new JLabel("Methods sampling:");
        methodsSamplingLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 7, 0, 0);
        instrSettingsPanel.add(methodsSamplingLabel, c);
        
        // samplingFrequencyContainer - definition
        JPanel samplingFrequencyContainer = new JPanel(new GridBagLayout());

        // samplingFrequencyLabel
        samplingFrequencyLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(samplingFrequencyLabel, Bundle.StpSamplingFrequencyLabel());
//        samplingFrequencyLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 15, 0, 5);
        samplingFrequencyContainer.add(samplingFrequencyLabel, c);

        // samplingFrequencySpinner
        samplingFrequencySpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
//        samplingFrequencySpinner.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
//        samplingFrequencySpinner.addChangeListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        samplingFrequencyContainer.add(samplingFrequencySpinner, c);

        // sampledTimingLabel
        samplingFrequencyUnitsLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(samplingFrequencyUnitsLabel, Bundle.StpSamplingFrequencyMs());
        samplingFrequencyUnitsLabel.setLabelFor(samplingFrequencySpinner);
//        samplingFrequencyUnitsLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        samplingFrequencyUnitsLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 5, 0, 0);
        samplingFrequencyContainer.add(samplingFrequencyUnitsLabel, c);

        // sampledTimingContainer - customization
        samplingFrequencyContainer.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 7, 15, 0);
        instrSettingsPanel.add(samplingFrequencyContainer, c);

        // methodsTrackingLabel
        methodsTrackingLabel = new JLabel(Bundle.CPUSettingsAdvancedPanel_MethodsTrackingLabelText());
        methodsTrackingLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 7, 0, 0);
        instrSettingsPanel.add(methodsTrackingLabel, c);

        // exactTimingRadio
        exactTimingRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(exactTimingRadio, Bundle.CPUSettingsAdvancedPanel_InstrRadioText());
//        exactTimingRadio.setToolTipText(Bundle.StpExactTimingTooltip());
        methodsTrackingRadiosGroup.add(exactTimingRadio);
//        exactTimingRadio.addActionListener(getSettingsChangeListener());
        exactTimingRadio.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 19, 0, 0);
        instrSettingsPanel.add(exactTimingRadio, c);

        // sampledTimingContainer - definition
        JPanel sampledTimingContainer = new JPanel(new GridBagLayout());

        // sampledTimingRadio
        sampledTimingRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(sampledTimingRadio, Bundle.CPUSettingsAdvancedPanel_SamplingRadioText());
//        sampledTimingRadio.setToolTipText(Bundle.StpSampledTimingTooltip());
        methodsTrackingRadiosGroup.add(sampledTimingRadio);
        sampledTimingRadio.setOpaque(false);
        sampledTimingRadio.setSelected(true);
        sampledTimingRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    sampledTimingSpinner.setEnabled(sampledTimingRadio.isSelected());
                    sampledTimingLabel.setEnabled(sampledTimingRadio.isSelected());
                }
            });
//        sampledTimingRadio.addActionListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 5);
        sampledTimingContainer.add(sampledTimingRadio, c);

        // sampledTimingSpinner
        sampledTimingSpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
//        sampledTimingSpinner.setToolTipText(Bundle.StpSampledTimingTooltip());
//        sampledTimingSpinner.addChangeListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        sampledTimingContainer.add(sampledTimingSpinner, c);

        // sampledTimingLabel
        sampledTimingLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(sampledTimingLabel, Bundle.StpSamplingFrequencyMs());
        sampledTimingLabel.setLabelFor(sampledTimingSpinner);
//        sampledTimingLabel.setToolTipText(Bundle.StpSampledTimingTooltip());
        sampledTimingLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 5, 0, 0);
        sampledTimingContainer.add(sampledTimingLabel, c);

        // sampledTimingContainer - customization
        sampledTimingContainer.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 19, 5, 0);
        instrSettingsPanel.add(sampledTimingContainer, c);

        // excludeTimeCheckbox
        excludeTimeCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(excludeTimeCheckbox, Bundle.CPUSettingsAdvancedPanel_ExcludeTimeCheckboxText());
//        excludeTimeCheckbox.setToolTipText(Bundle.StpSleepWaitTooltip());
//        excludeTimeCheckbox.addActionListener(getSettingsChangeListener());
        excludeTimeCheckbox.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(excludeTimeCheckbox, c);

        // profileFrameworkCheckbox
        profileFrameworkCheckbox = new JCheckBox() {
            public void setSelected(boolean b) {
                super.setSelected(b);
//                updateEnabling();
            }
        };
        org.openide.awt.Mnemonics.setLocalizedText(profileFrameworkCheckbox, Bundle.CPUSettingsAdvancedPanel_ProfileFrameworkCheckboxText());
//        profileFrameworkCheckbox.setToolTipText(Bundle.StpFrameworkTooltip());
        profileFrameworkCheckbox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
//                    updateEnabling();
                }
            });
//        profileFrameworkCheckbox.addActionListener(getSettingsChangeListener());
        profileFrameworkCheckbox.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(profileFrameworkCheckbox, c);

        // profileSpawnedThreadsCheckbox
        profileSpawnedThreadsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(profileSpawnedThreadsCheckbox, Bundle.CPUSettingsAdvancedPanel_ProfileThreadsCheckboxText());
//        profileSpawnedThreadsCheckbox.setToolTipText(Bundle.StpSpawnedTooltip());
//        profileSpawnedThreadsCheckbox.addActionListener(getSettingsChangeListener());
        profileSpawnedThreadsCheckbox.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(profileSpawnedThreadsCheckbox, c);

        // limitThreadsContainer - definition
        JPanel limitThreadsContainer = new JPanel(new GridBagLayout());

        // limitThreadsCheckbox
        limitThreadsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(limitThreadsCheckbox, Bundle.CPUSettingsAdvancedPanel_LimitThreadsCheckboxText());
//        limitThreadsCheckbox.setToolTipText(Bundle.StpLimitThreadsTooltip());
//        limitThreadsCheckbox.addActionListener(getSettingsChangeListener());
        limitThreadsCheckbox.setOpaque(false);
        limitThreadsCheckbox.setSelected(true);
        limitThreadsCheckbox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    limitThreadsSpinner.setEnabled(limitThreadsCheckbox.isSelected());
                }
            });
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 5);
        limitThreadsContainer.add(limitThreadsCheckbox, c);

        // limitThreadsSpinner
        limitThreadsSpinner = new JExtendedSpinner(new SpinnerNumberModel(50, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
//        limitThreadsSpinner.setToolTipText(Bundle.StpLimitThreadsTooltip());
//        limitThreadsSpinner.addChangeListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        limitThreadsContainer.add(limitThreadsSpinner, c);

        // limitThreadsContainer - customization
        limitThreadsContainer.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(limitThreadsContainer, c);

        // useCPUTimerCheckbox
        useCPUTimerCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(useCPUTimerCheckbox, Bundle.CPUSettingsAdvancedPanel_ThreadTimerCheckboxText());
//        useCPUTimerCheckbox.setToolTipText(Bundle.StpCpuTimerTooltip());
//        useCPUTimerCheckbox.addActionListener(getSettingsChangeListener());
        useCPUTimerCheckbox.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(useCPUTimerCheckbox, c);

        // instrumentationSchemeContainer - definition
        JPanel instrumentationSchemeContainer = new JPanel(new GridBagLayout());

        // instrumentationSchemeLabel
        instrumentationSchemeLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentationSchemeLabel, Bundle.CPUSettingsAdvancedPanel_InstrSchemeLabelText());
//        instrumentationSchemeLabel.setToolTipText(Bundle.StpInstrSchemeTooltip());
        instrumentationSchemeLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 5);
        instrumentationSchemeContainer.add(instrumentationSchemeLabel, c);

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
//        instrumentationSchemeCombo.setToolTipText(Bundle.StpInstrSchemeTooltip());
//        instrumentationSchemeCombo.addActionListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        instrumentationSchemeContainer.add(instrumentationSchemeCombo, c);

        // instrumentationSchemeContainer - customization
        instrumentationSchemeContainer.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 2, 0);
        instrSettingsPanel.add(instrumentationSchemeContainer, c);

        // instrumentLabel
        instrumentLabel = new JLabel(Bundle.CPUSettingsAdvancedPanel_InstrumentLabelText());
        instrumentLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 11;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 0, 0);
        instrSettingsPanel.add(instrumentLabel, c);

        // instrumentMethodInvokeCheckbox
        instrumentMethodInvokeCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentMethodInvokeCheckbox, Bundle.CPUSettingsAdvancedPanel_MethodInvokeCheckboxText());
//        instrumentMethodInvokeCheckbox.setToolTipText(Bundle.StpMethodInvokeTooltip());
//        instrumentMethodInvokeCheckbox.addActionListener(getSettingsChangeListener());
        instrumentMethodInvokeCheckbox.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 12;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 19, 0, 0);
        instrSettingsPanel.add(instrumentMethodInvokeCheckbox, c);

        // instrumentGettersSettersCheckbox
        instrumentGettersSettersCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentGettersSettersCheckbox, Bundle.CPUSettingsAdvancedPanel_GetterSetterCheckboxText());
//        instrumentGettersSettersCheckbox.setToolTipText(Bundle.StpGetterSetterTooltip());
//        instrumentGettersSettersCheckbox.addActionListener(getSettingsChangeListener());
        instrumentGettersSettersCheckbox.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 13;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 19, 0, 0);
        instrSettingsPanel.add(instrumentGettersSettersCheckbox, c);

        // instrumentEmptyMethodsCheckbox
        instrumentEmptyMethodsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(instrumentEmptyMethodsCheckbox, Bundle.CPUSettingsAdvancedPanel_EmptyMethodsCheckboxText());
//        instrumentEmptyMethodsCheckbox.setToolTipText(Bundle.StpEmptyMethodsTooltip());
//        instrumentEmptyMethodsCheckbox.addActionListener(getSettingsChangeListener());
        instrumentEmptyMethodsCheckbox.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 14;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 19, 3, 0);
        instrSettingsPanel.add(instrumentEmptyMethodsCheckbox, c);
        
        y = 15;
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, htab, vgap, 0);
        add(instrSettingsPanel, c);
        
        Separator memorySettingsSeparator = new Separator("Memory Settings");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(memorySettingsSeparator, c);
        
        methodsTrackingRadiosGroup = new ButtonGroup();

        // settingsPanel
        memorySettingsPanel = new JPanel(new GridBagLayout());
        memorySettingsPanel.setOpaque(false);
//        settingsPanel.setBorder(BorderFactory.createTitledBorder(Bundle.MemorySettingsAdvancedPanel_SettingsCaption()));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 5, 10, 5);
        add(memorySettingsPanel, c);
        
        // trackEveryContainer - definition
        JPanel trackEveryContainer = new JPanel(new GridBagLayout());

        // trackEveryLabel1
        trackEveryLabel1 = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(trackEveryLabel1, Bundle.MemorySettingsBasicPanel_TrackEveryLabelText());
//        trackEveryLabel1.setToolTipText(Bundle.StpTrackEveryTooltip());
        trackEveryLabel1.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 5);
        trackEveryContainer.add(trackEveryLabel1, c);

        // trackEverySpinner
        trackEverySpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        trackEveryLabel1.setLabelFor(trackEverySpinner);
//        trackEverySpinner.setToolTipText(Bundle.StpTrackEveryTooltip());
//        trackEverySpinner.addChangeListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        trackEveryContainer.add(trackEverySpinner, c);

        // trackEveryLabel2
        trackEveryLabel2 = new JLabel(Bundle.MemorySettingsBasicPanel_AllocLabelText());
//        trackEveryLabel2.setToolTipText(Bundle.StpTrackEveryTooltip());
        trackEveryLabel2.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 5, 0, 0);
        trackEveryContainer.add(trackEveryLabel2, c);

        // trackEveryContainer - customization
        trackEveryContainer.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 7, 5, 0);
        memorySettingsPanel.add(trackEveryContainer, c);

        // recordStackTracesLabel
        recordStackTracesLabel = new JLabel(Bundle.MemorySettingsAdvancedPanel_RecordTracesLabelText());
        recordStackTracesLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 0, 0);
        memorySettingsPanel.add(recordStackTracesLabel, c);

        // fullDepthRadio
        fullDepthRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(fullDepthRadio, Bundle.MemorySettingsAdvancedPanel_FullStackRadioText());
//        fullDepthRadio.setToolTipText(Bundle.StpFullDepthTooltip());
        methodsTrackingRadiosGroup.add(fullDepthRadio);
//        fullDepthRadio.addActionListener(getSettingsChangeListener());
        fullDepthRadio.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 19, 0, 0);
        memorySettingsPanel.add(fullDepthRadio, c);

        // sampledTimingContainer - definition
        sampledTimingContainer = new JPanel(new GridBagLayout());

        // definedDepthRadio
        definedDepthRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(definedDepthRadio, Bundle.MemorySettingsAdvancedPanel_LimitStackRadioText());
//        definedDepthRadio.setToolTipText(Bundle.StpLimitDepthTooltip());
        methodsTrackingRadiosGroup.add(definedDepthRadio);
        definedDepthRadio.setOpaque(false);
        definedDepthRadio.setSelected(true);
        definedDepthRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
//                    updateEnabling();
                }
            });
//        definedDepthRadio.addActionListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 5);
        sampledTimingContainer.add(definedDepthRadio, c);

        // defineDepthSpinner
        defineDepthSpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1)) {
                public Dimension getPreferredSize() {
                    return new Dimension(55, getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
//        defineDepthSpinner.addChangeListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        sampledTimingContainer.add(defineDepthSpinner, c);

        // defineDepthLabel
        defineDepthLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(defineDepthLabel, Bundle.MemorySettingsAdvancedPanel_FramesLabelText());
        defineDepthLabel.setLabelFor(defineDepthSpinner);
//        defineDepthSpinner.setToolTipText(Bundle.StpLimitDepthTooltip());
//        defineDepthLabel.setToolTipText(Bundle.StpLimitDepthTooltip());
        defineDepthLabel.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 5, 0, 0);
        sampledTimingContainer.add(defineDepthLabel, c);

        // sampledTimingContainer - customization
        sampledTimingContainer.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 19, 5, 0);
        memorySettingsPanel.add(sampledTimingContainer, c);

        // runGCCheckbox
        runGCCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(runGCCheckbox, Bundle.MemorySettingsAdvancedPanel_RunGcCheckboxText());
//        runGCCheckbox.setToolTipText(Bundle.StpRunGcTooltip());
        runGCCheckbox.setOpaque(false);
//        runGCCheckbox.addActionListener(getSettingsChangeListener());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 7, 3, 0);
        memorySettingsPanel.add(runGCCheckbox, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, htab, vgap, 0);
        add(memorySettingsPanel, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        add(UIUtils.createFillerPanel(), c);
        
    }
    
    
    // --- CPU -----------------------------------------------------------------
    
    private JCheckBox excludeTimeCheckbox;
    private JCheckBox instrumentEmptyMethodsCheckbox;
    private JCheckBox instrumentGettersSettersCheckbox;
    private JCheckBox instrumentMethodInvokeCheckbox;
    private JCheckBox limitThreadsCheckbox;
    private JCheckBox profileFrameworkCheckbox;
    private JCheckBox profileSpawnedThreadsCheckbox;
    private JCheckBox useCPUTimerCheckbox;
    private JComboBox instrumentationSchemeCombo;
    private JLabel instrumentLabel;
    private JLabel instrumentationSchemeLabel;
    private JLabel methodsTrackingLabel;
    private JLabel sampledTimingLabel;
    private JLabel samplingFrequencyLabel;
    private JLabel samplingFrequencyUnitsLabel;
    private JPanel cpuSettingsPanel;
    private JPanel samplSettingsPanel;
    private JPanel instrSettingsPanel;
    private JRadioButton exactTimingRadio;
    private JRadioButton sampledTimingRadio;
    private JSpinner limitThreadsSpinner;
    private JSpinner sampledTimingSpinner;
    private JSpinner samplingFrequencySpinner;
    
    
    // --- Memory --------------------------------------------------------------
    
    private JCheckBox runGCCheckbox;
    private JLabel defineDepthLabel;
    private JLabel recordStackTracesLabel;
    private JPanel memorySettingsPanel;
    private JRadioButton definedDepthRadio;
    private JRadioButton fullDepthRadio;
    private JSpinner defineDepthSpinner;
    private JLabel trackEveryLabel1;
    private JLabel trackEveryLabel2;
    private JSpinner trackEverySpinner;
    
}
