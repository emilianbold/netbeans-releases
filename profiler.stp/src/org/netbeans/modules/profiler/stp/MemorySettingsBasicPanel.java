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

import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.stp.ui.HyperlinkLabel;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "MemorySettingsBasicPanel_AllocRadioText=&Quick (sampled)",
    "MemorySettingsBasicPanel_LivenessRadioText=&Advanced (instrumented)",
    "StpFullLifecycleText=&Record full object lifecycle",
    "MemorySettingsBasicPanel_TrackEveryLabelText=&Track every",
//# Used as Track every [JSpinner] object allocations
    "MemorySettingsBasicPanel_AllocLabelText=object allocations",
    "MemorySettingsBasicPanel_RecordTracesCheckboxText=R&ecord stack trace for allocations",
    "MemorySettingsBasicPanel_UsePpsCheckboxText=&Use defined Profiling Points",
    "MemorySettingsBasicPanel_ShowPpsString=Show active Profiling Points"
})
public class MemorySettingsBasicPanel extends DefaultSettingsPanel implements HelpCtx.Provider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // --- Instance variables declaration ----------------------------------------
    private static final String HELP_CTX_KEY = "MemorySettings.Basic.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HyperlinkLabel profilingPointsLink;
    private JCheckBox profilingPointsCheckbox;
    private JCheckBox recordStackTraceCheckbox;
    private JCheckBox fullDataCheckbox;

    // --- UI components declaration ---------------------------------------------
    private JRadioButton sampleAppRadio;
    private JRadioButton profileAppRadio;
    private Lookup.Provider project; // TODO: implement reset or remove!!!
    private Runnable profilingPointsDisplayer;
    
    private boolean lastFullDataState;
    private boolean fullDataStateCache;
    private boolean lastStackTracesState;
    private boolean stackTracesStateCache;
    private boolean lastProfilingPointsState;
    private boolean profilingPointsStateCache;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public MemorySettingsBasicPanel() {
        super();
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setContext(Lookup.Provider project, Runnable profilingPointsDisplayer) {
        this.project = project;
        this.profilingPointsDisplayer = profilingPointsDisplayer;
        updateControls();
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public void setProfilingType(int profilingType) {
        sampleAppRadio.setSelected(profilingType == ProfilingSettings.PROFILE_MEMORY_SAMPLING);
        profileAppRadio.setSelected(profilingType == ProfilingSettings.PROFILE_MEMORY_ALLOCATIONS ||
                                    profilingType == ProfilingSettings.PROFILE_MEMORY_LIVENESS);
    }

    public int getProfilingType() {
        if (sampleAppRadio.isSelected()) {
            return ProfilingSettings.PROFILE_MEMORY_SAMPLING;
        } else if (fullDataCheckbox.isSelected()) {
            return ProfilingSettings.PROFILE_MEMORY_LIVENESS;
        } else {
            return ProfilingSettings.PROFILE_MEMORY_ALLOCATIONS;
        }
    }

    public void setRecordStackTrace(boolean record) {
        recordStackTraceCheckbox.setSelected(record);
    }

    public boolean getRecordStackTrace() {
        return recordStackTraceCheckbox.isSelected();
    }

    public void setUseProfilingPoints(boolean use) {
        profilingPointsCheckbox.setSelected(use && profilingPointsCheckbox.isEnabled());
        updateControls();
    }

    public boolean getUseProfilingPoints() {
        return profilingPointsCheckbox.isSelected();
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
        JPanel contents = new MemorySettingsBasicPanel();
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

        ButtonGroup memoryModeRadios = new ButtonGroup();

        // sampleAppRadio
        sampleAppRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(sampleAppRadio, Bundle.MemorySettingsBasicPanel_AllocRadioText());
        sampleAppRadio.setToolTipText(Bundle.StpAllocTooltip());
        sampleAppRadio.setOpaque(false);
        sampleAppRadio.setSelected(true);
        memoryModeRadios.add(sampleAppRadio);
        sampleAppRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateControls();
                }
            });
        sampleAppRadio.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 30, 0, 0);
        add(sampleAppRadio, constraints);

        // profileAppRadio
        profileAppRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(profileAppRadio, Bundle.MemorySettingsBasicPanel_LivenessRadioText());
        profileAppRadio.setToolTipText(Bundle.StpLivenessTooltip());
        profileAppRadio.setOpaque(false);
        profileAppRadio.setSelected(false);
        memoryModeRadios.add(profileAppRadio);
        profileAppRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateControls();
                }
            });
        profileAppRadio.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 30, 0, 0);
        add(profileAppRadio, constraints);        
        
        //
        fullDataCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(fullDataCheckbox, Bundle.StpFullLifecycleText());
        fullDataCheckbox.setToolTipText(Bundle.StpFullLifecycleTooltip());
        fullDataCheckbox.setOpaque(false);
        fullDataCheckbox.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(25, 25, 0, 0);
        add(fullDataCheckbox, constraints);
        //
        

        // recordStackTraceCheckbox
        recordStackTraceCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(recordStackTraceCheckbox, Bundle.MemorySettingsBasicPanel_RecordTracesCheckboxText());
        recordStackTraceCheckbox.setToolTipText(Bundle.StpStackTraceTooltip());
        recordStackTraceCheckbox.addActionListener(getSettingsChangeListener());
        recordStackTraceCheckbox.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 25, 0, 0);
        add(recordStackTraceCheckbox, constraints);

        // fillerPanel
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(UIUtils.createFillerPanel(), constraints);

        // profilingPointsContainer - definition
        JPanel profilingPointsContainer = new JPanel(new GridBagLayout());

        // profilingPointsCheckbox
        profilingPointsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(profilingPointsCheckbox, Bundle.MemorySettingsBasicPanel_UsePpsCheckboxText());
        profilingPointsCheckbox.setToolTipText(Bundle.StpUsePpsTooltip());
        profilingPointsCheckbox.setOpaque(false);
        profilingPointsCheckbox.setSelected(true);
        profilingPointsStateCache = profilingPointsCheckbox.isSelected();
        profilingPointsCheckbox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateEnabling();
                }
            });
        profilingPointsCheckbox.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 10);
        profilingPointsContainer.add(profilingPointsCheckbox, constraints);

        // profilingPointsLink
        Color linkColor = Color.RED;
        String colorText = "rgb(" + linkColor.getRed() + "," + linkColor.getGreen() + "," + linkColor.getBlue() + ")"; //NOI18N
        profilingPointsLink = new HyperlinkLabel("<nobr><a href='#'>" + Bundle.MemorySettingsBasicPanel_ShowPpsString() + "</a></nobr>", //NOI18N
                                                 "<nobr><a href='#' color=\"" + colorText + "\">" + Bundle.MemorySettingsBasicPanel_ShowPpsString()
                                                 + "</a></nobr>", //NOI18N
                                                 new Runnable() {
                public void run() {
                    performShowProfilingPointsAction();
                }
            });
        profilingPointsLink.setToolTipText(Bundle.StpShowPpsTooltip());
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        profilingPointsContainer.add(profilingPointsLink, constraints);

        // profilingPointsContainer - customization
        profilingPointsContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(20, 25, 10, 0);
        add(profilingPointsContainer, constraints);
    }

    private void performShowProfilingPointsAction() {
        profilingPointsDisplayer.run();
    }

    private void updateEnabling() {
        profilingPointsLink.setEnabled(profilingPointsCheckbox.isSelected() && profilingPointsCheckbox.isEnabled());
    }
    
    private void updateControls() {
        if (sampleAppRadio.isSelected()) {
            if (!lastFullDataState) {
                fullDataStateCache = fullDataCheckbox.isSelected();
                fullDataCheckbox.setEnabled(false);
                fullDataCheckbox.setSelected(false);
                lastFullDataState = true;
            }
            if (!lastStackTracesState) {
                stackTracesStateCache = recordStackTraceCheckbox.isSelected();
                recordStackTraceCheckbox.setEnabled(false);
                recordStackTraceCheckbox.setSelected(false);
                lastStackTracesState = true;
            }
            profilingPointsCheckbox.setEnabled(false);
            if (project == null) return;
            if (!lastProfilingPointsState) {
                profilingPointsStateCache = profilingPointsCheckbox.isSelected();
                profilingPointsCheckbox.setSelected(false);
                lastProfilingPointsState = true;
            }
        } else {
            if (lastFullDataState) {
                fullDataCheckbox.setEnabled(true);
                fullDataCheckbox.setSelected(fullDataStateCache);
                lastFullDataState = false;
            }
            if (lastStackTracesState) {
                recordStackTraceCheckbox.setEnabled(true);
                recordStackTraceCheckbox.setSelected(stackTracesStateCache);
                lastStackTracesState = false;
            }
            profilingPointsCheckbox.setEnabled(project != null);
            if (project == null) return;
            if (lastProfilingPointsState) {
                profilingPointsCheckbox.setSelected(profilingPointsStateCache);
                lastProfilingPointsState = false;
            }
        }
    }
    
}
