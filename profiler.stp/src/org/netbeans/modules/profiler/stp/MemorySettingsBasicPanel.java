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
    "MemorySettingsBasicPanel_AllocRadioText=&Record object creation only",
    "MemorySettingsBasicPanel_LivenessRadioText=Record &both object creation and garbage collection",
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
    private JLabel trackEveryLabel1;
    private JLabel trackEveryLabel2;

    // --- UI components declaration ---------------------------------------------
    private JRadioButton allocationsRadio;
    private JRadioButton livenessRadio;
    private JSpinner trackEverySpinner;
    private Lookup.Provider project; // TODO: implement reset or remove!!!
    private Runnable profilingPointsDisplayer;

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
        updateProject(project);
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public void setProfilingType(int profilingType) {
        allocationsRadio.setSelected(profilingType == ProfilingSettings.PROFILE_MEMORY_ALLOCATIONS);
        livenessRadio.setSelected(profilingType == ProfilingSettings.PROFILE_MEMORY_LIVENESS);
    }

    public int getProfilingType() {
        if (allocationsRadio.isSelected()) {
            return ProfilingSettings.PROFILE_MEMORY_ALLOCATIONS;
        } else {
            return ProfilingSettings.PROFILE_MEMORY_LIVENESS;
        }
    }

    public void setRecordStackTrace(boolean record) {
        recordStackTraceCheckbox.setSelected(record);
    }

    public boolean getRecordStackTrace() {
        return recordStackTraceCheckbox.isSelected();
    }

    public void setTrackEvery(int trackEvery) {
        trackEverySpinner.setValue(Integer.valueOf(trackEvery));
    }

    public int getTrackEvery() {
        return ((Integer) trackEverySpinner.getValue()).intValue();
    }

    public void setUseProfilingPoints(boolean use) {
        profilingPointsCheckbox.setSelected(use);
        updateEnabling();
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

        // allocationsRadio
        allocationsRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(allocationsRadio, Bundle.MemorySettingsBasicPanel_AllocRadioText());
        allocationsRadio.setToolTipText(Bundle.StpAllocTooltip());
        allocationsRadio.setOpaque(false);
        allocationsRadio.setSelected(true);
        memoryModeRadios.add(allocationsRadio);
        allocationsRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                }
            });
        allocationsRadio.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 30, 0, 0);
        add(allocationsRadio, constraints);

        // livenessRadio
        livenessRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(livenessRadio, Bundle.MemorySettingsBasicPanel_LivenessRadioText());
        livenessRadio.setToolTipText(Bundle.StpLivenessTooltip());
        livenessRadio.setOpaque(false);
        livenessRadio.setSelected(true);
        memoryModeRadios.add(livenessRadio);
        livenessRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                }
            });
        livenessRadio.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 30, 0, 0);
        add(livenessRadio, constraints);

        // trackEveryContainer - definition
        JPanel trackEveryContainer = new JPanel(new GridBagLayout());

        // trackEveryLabel1
        trackEveryLabel1 = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(trackEveryLabel1, Bundle.MemorySettingsBasicPanel_TrackEveryLabelText());
        trackEveryLabel1.setToolTipText(Bundle.StpTrackEveryTooltip());
        trackEveryLabel1.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 5);
        trackEveryContainer.add(trackEveryLabel1, constraints);

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
        trackEverySpinner.setToolTipText(Bundle.StpTrackEveryTooltip());
        trackEverySpinner.addChangeListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        trackEveryContainer.add(trackEverySpinner, constraints);

        // trackEveryLabel2
        trackEveryLabel2 = new JLabel(Bundle.MemorySettingsBasicPanel_AllocLabelText());
        trackEveryLabel2.setToolTipText(Bundle.StpTrackEveryTooltip());
        trackEveryLabel2.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 5, 0, 0);
        trackEveryContainer.add(trackEveryLabel2, constraints);

        // trackEveryContainer - customization
        trackEveryContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(20, 25, 0, 0);
        add(trackEveryContainer, constraints);

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
        constraints.insets = new Insets(20, 25, 0, 0);
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

    // --- Private implementation ------------------------------------------------
    private void updateProject(final Lookup.Provider project) {
        profilingPointsCheckbox.setEnabled(project != null);
    }
}
