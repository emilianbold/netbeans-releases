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

import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.filters.DefinedFilterSets;
import org.netbeans.lib.profiler.common.filters.FilterUtils;
import org.netbeans.lib.profiler.common.filters.SimpleFilter;
import org.netbeans.lib.profiler.ui.components.JExtendedComboBox;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.lib.profiler.ui.SwingWorker;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProfilingRoots;
import org.netbeans.modules.profiler.api.ProgressDisplayer;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.project.ProjectContentsSupport;
import org.netbeans.modules.profiler.stp.ui.FilterSetsPanel;
import org.netbeans.modules.profiler.stp.ui.GlobalFiltersPanel;
import org.netbeans.modules.profiler.stp.ui.HyperlinkLabel;
import org.netbeans.modules.profiler.stp.ui.PreferredInstrFilterPanel;
import org.netbeans.modules.profiler.stp.ui.QuickFilterPanel;
import org.netbeans.modules.profiler.ui.ProfilerProgressDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "CPUSettingsBasicPanel_DefaultRootsString=<font color=\"{0}\">default profiling roots, </font><a href=\"#\" {1}>customize...</a>",
    "CPUSettingsBasicPanel_DefaultRootsToolTip=Click the link to define custom profiling roots.",
    "CPUSettingsBasicPanel_CustomRootsString=<font color=\"{0}\">custom profiling roots, </font><a href=\"#\" {1}>edit...</a>",
    "CPUSettingsBasicPanel_CustomRootsToolTip=Click the link to customize defined profiling roots.",
    "CPUSettingsBasicPanel_QuickFilterDialogCaption=Set Quick Filter",
    "CPUSettingsBasicPanel_FilterSetsDialogCaption=Customize Filter Sets",
    "CPUSettingsBasicPanel_GlobalFiltersDialogCaption=Edit Global Filters",
    "CPUSettingsBasicPanel_SampleAppRadioText=&Quick (sampled)",
    "CPUSettingsBasicPanel_ProfileAppRadioText=&Advanced (instrumented)",
    "CPUSettingsBasicPanel_StopwatchRadioText=Stopwatch",
    "CPUSettingsBasicPanel_FilterLabelText=&Filter:",
    "CPUSettingsBasicPanel_ShowFilterString=Show filter value",
    "CPUSettingsBasicPanel_EditFilterString=Edit filter value",
    "CPUSettingsBasicPanel_EditFilterSetString=Edit filter sets",
    "CPUSettingsBasicPanel_UsePpsCheckboxText=&Use defined Profiling Points",
    "CPUSettingsBasicPanel_ShowPpsString=Show active Profiling Points",
    "CPUSettingsBasicPanel_EditGlobalFilterString=Edit G&lobal Filters...",
    "CPUSettingsBasicPanel_ShowFilterCaption=Filter Value: {0}"
})
public class CPUSettingsBasicPanel extends DefaultSettingsPanel implements ActionListener, PopupMenuListener, HelpCtx.Provider {

    // --- Instance variables declaration ----------------------------------------
    private static final String HELP_CTX_KEY = "CPUSettings.Basic.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HyperlinkLabel editFilterLink;
    private HyperlinkLabel editFilterSetsLink;
    private HyperlinkLabel partOfAppHintLink;
    private HyperlinkLabel profilingPointsLink;
    private HyperlinkLabel showFilterLink;
    private HyperlinkLabel stopwatchHintLink;
    private JButton globalFiltersButton;
    private JCheckBox profilingPointsCheckbox;
    private JComboBox filterCombo;
    private JLabel filterLabel;

    // --- UI components declaration ---------------------------------------------
    private JRadioButton sampleAppRadio;
    private JRadioButton profileAppRadio;
    private JRadioButton stopwatchRadio;
    private List<SimpleFilter> preferredInstrFilters;
    private Object selectedInstrumentationFilter = SimpleFilter.NO_FILTER;
    private Lookup.Provider project; // TODO: implement reset or remove!!!
    private Runnable profilingPointsDisplayer;
    private SimpleFilter quickFilter;
    private ClientUtils.SourceCodeSelection[] rootMethods = new ClientUtils.SourceCodeSelection[0];

    // --- ActionListener & PopupMenuListener implementation ---------------------
    private boolean filterComboBoxPopupCancelled = false;
    private boolean lastProfilingPointsState;
    private boolean profilingPointsStateCache;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public CPUSettingsBasicPanel() {
        super();
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setContext(Lookup.Provider project, List<SimpleFilter> preferredInstrFilters, Runnable profilingPointsDisplayer) {
        this.project = project;
        this.profilingPointsDisplayer = profilingPointsDisplayer;
        this.preferredInstrFilters = preferredInstrFilters;
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public void setInstrumentationFilter(Object instrumentationFilter) {
        selectedInstrumentationFilter = instrumentationFilter;
        updateFilterComboBoxItems();
        selectActiveComboItem();
        updateControls();
    }

    public Object getInstrumentationFilter() {
        return selectedInstrumentationFilter;
    }

    public void setProfilingType(int profilingType) {
        sampleAppRadio.setSelected(profilingType == ProfilingSettings.PROFILE_CPU_SAMPLING);
        profileAppRadio.setSelected(profilingType == ProfilingSettings.PROFILE_CPU_ENTIRE ||
                                       profilingType == ProfilingSettings.PROFILE_CPU_PART);
        stopwatchRadio.setSelected(profilingType == ProfilingSettings.PROFILE_CPU_STOPWATCH);
        profilingPointsCheckbox.setEnabled(project != null && profileAppRadio.isSelected());
    }

    public int getProfilingType() {
        if (sampleAppRadio.isSelected()) {
            return ProfilingSettings.PROFILE_CPU_SAMPLING;
        } else if (profileAppRadio.isSelected()) {
            return rootMethods.length == 0 ? ProfilingSettings.PROFILE_CPU_ENTIRE :
                                             ProfilingSettings.PROFILE_CPU_PART;
        } else {
            return ProfilingSettings.PROFILE_CPU_STOPWATCH;
        }
    }

    public void setQuickFilter(SimpleFilter quickFilter) {
        this.quickFilter = quickFilter;
    }

    public SimpleFilter getQuickFilter() {
        return quickFilter;
    }

    public void setRootMethods(ClientUtils.SourceCodeSelection[] rootMethods) {
        this.rootMethods = rootMethods;
    }

    public ClientUtils.SourceCodeSelection[] getRootMethods() {
        return rootMethods;
    }

    public void setUseProfilingPoints(boolean use) {
        profilingPointsCheckbox.setSelected(use && profilingPointsCheckbox.isEnabled());
        updateEnabling();
    }

    public boolean getUseProfilingPoints() {
        return profilingPointsCheckbox.isSelected();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == globalFiltersButton) {
            performEditGlobalFiltersAction();
        } else if (e.getSource() == filterCombo) {
            updateComboLinks();
            updateControls();
        }
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
        } catch (Exception e) {}

        JFrame frame = new JFrame("Tester Frame"); //NOI18N
        JPanel contents = new CPUSettingsBasicPanel();
        contents.setPreferredSize(new Dimension(375, 245));
        frame.getContentPane().add(contents);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void popupMenuCanceled(final PopupMenuEvent e) {
        filterComboBoxPopupCancelled = true;
    }

    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
        if (!filterComboBoxPopupCancelled && quickFilter != null && quickFilter.equals(filterCombo.getSelectedItem())) {
            filterCombo.removePopupMenuListener(this);
            filterCombo.hidePopup();
            filterCombo.addPopupMenuListener(this);

            SwingUtilities.invokeLater(new Runnable() { // use SwingUtilities.invokeLater to let the filterComboBoxPopup close
                    public void run() {
                        performQuickFilterAction();
                    }
                });
        }

        filterComboBoxPopupCancelled = false;
    }

    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
    }

    // --- UI definition ---------------------------------------------------------
    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        ButtonGroup cpuModeRadios = new ButtonGroup();

        // entireAppRadio
        sampleAppRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(sampleAppRadio, Bundle.CPUSettingsBasicPanel_SampleAppRadioText());
        sampleAppRadio.setToolTipText(Bundle.StpSampleAppTooltip());
        sampleAppRadio.setOpaque(false);
        sampleAppRadio.setSelected(true);
        cpuModeRadios.add(sampleAppRadio);
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

        // partOfAppContainer - definition
        JPanel partOfAppContainer = new JPanel(new GridBagLayout()) {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width,
                                     Math.max(profileAppRadio.getPreferredSize().height,
                                              partOfAppHintLink.getPreferredSize().height));
            }
        };

        // partOfAppRadio
        profileAppRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(profileAppRadio, Bundle.CPUSettingsBasicPanel_ProfileAppRadioText());
        profileAppRadio.setToolTipText(Bundle.StpProfileAppTooltip());
        profileAppRadio.setOpaque(false);
        profileAppRadio.setSelected(true);
        cpuModeRadios.add(profileAppRadio);
        profileAppRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateControls();
                }
            });
        profileAppRadio.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        partOfAppContainer.add(profileAppRadio, constraints);

        // partOfAppHintLink
        Color linkColor = Color.RED;
        String colorText = "rgb(" + linkColor.getRed() + "," + linkColor.getGreen() + "," + linkColor.getBlue() + ")"; //NOI18N
        String textColorText = "rgb(" + Color.GRAY.getRed() + "," + Color.GRAY.getGreen() + "," + Color.GRAY.getBlue() + ")"; //NOI18N
        String labelText = "<nobr>" + Bundle.CPUSettingsBasicPanel_DefaultRootsString(textColorText, "") + "</nobr>"; //NOI18N
        String labelFocusedText = "<nobr>" //NOI18N
                                   + Bundle.CPUSettingsBasicPanel_DefaultRootsString(textColorText, "color=\"" + colorText + "\"") //NOI18N
                                   + "</nobr>"; //NOI18N
        partOfAppHintLink = new HyperlinkLabel(labelText, labelFocusedText,
                                               new Runnable() {
                public void run() {
                    performRootMethodsAction();
                }
            });
        partOfAppHintLink.setToolTipText(Bundle.CPUSettingsBasicPanel_DefaultRootsToolTip());
//        partOfAppHintLink.setVisible(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        partOfAppContainer.add(partOfAppHintLink, constraints);

        // partOfAppContainer - customization
        partOfAppContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 30, 0, 0);
        add(partOfAppContainer, constraints);

        // stopwatchContainer - definition
        JPanel stopwatchContainer = new JPanel(new GridBagLayout());

        // stopwatchRadio
        stopwatchRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(stopwatchRadio, Bundle.CPUSettingsBasicPanel_StopwatchRadioText());
        stopwatchRadio.setOpaque(false);
        stopwatchRadio.setSelected(true);
        cpuModeRadios.add(stopwatchRadio);
        stopwatchRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateControls();
                }
            });
        stopwatchRadio.addActionListener(getSettingsChangeListener());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        stopwatchContainer.add(stopwatchRadio, constraints);

        // stopwatchHintLink
        labelText = "<nobr><font color='" + textColorText + "'>2 stopwatches defined, </font><a href='#'>view</a></nobr>"; // NOI18N (currently not used)
        labelFocusedText = "<nobr><font color='" + textColorText + "'>2 stopwatches defined, </font><a href='#' color=\""
                           + colorText + "\">view</a></nobr>"; // NOI18N (currently not used)
        stopwatchHintLink = new HyperlinkLabel(labelText, labelFocusedText,
                                               new Runnable() {
                public void run() {
                    performRootMethodsAction();
                }
            }); /* TODO: show root methods selector */
        stopwatchHintLink.setVisible(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 0, 0);
        stopwatchContainer.add(stopwatchHintLink, constraints);

        // stopwatchContainer - customization
        stopwatchContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 30, 0, 0);
        add(stopwatchContainer, constraints);
        stopwatchContainer.setVisible(false); // TODO: unhide once stopwatch - codefragment is implemented

        // filterLabel
        filterLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, Bundle.CPUSettingsBasicPanel_FilterLabelText());
        filterLabel.setToolTipText(Bundle.StpFilterTooltip());
        filterLabel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(25, 25, 0, 0);
        add(filterLabel, constraints);

        // filterCombo
        filterCombo = new JExtendedComboBox() {
                public int getPreferredWidth() {
                    int preferredWidth = super.getPreferredSize().width;
                    preferredWidth = Math.max(preferredWidth, 220);
                    preferredWidth = Math.min(preferredWidth, 300);

                    return preferredWidth;
                }

                public Dimension getPreferredSize() {
                    return new Dimension(getPreferredWidth(), super.getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        filterLabel.setLabelFor(filterCombo);
        filterCombo.setToolTipText(Bundle.StpFilterTooltip());
        filterCombo.addActionListener(this);
        filterCombo.addActionListener(getSettingsChangeListener());
        filterCombo.addPopupMenuListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(25, 5, 0, 0);
        add(filterCombo, constraints);

        // filterLinksContainer - definition
        JPanel filterLinksContainer = new JPanel(new GridBagLayout());

        // showFilterLink
        labelText = "<nobr><a href='#'>" + Bundle.CPUSettingsBasicPanel_ShowFilterString() + "</a></nobr>"; //NOI18N
        labelFocusedText = "<nobr><a href='#' color=\"" + colorText + "\">" + Bundle.CPUSettingsBasicPanel_ShowFilterString() + "</a></nobr>"; //NOI18N
        showFilterLink = new HyperlinkLabel(labelText, labelFocusedText,
                                            new Runnable() {
                public void run() {
                    performShowFilterAction();
                }
            });
        showFilterLink.setToolTipText(Bundle.StpShowFilterTooltip());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 10);
        filterLinksContainer.add(showFilterLink, constraints);

        // editFilterLink
        labelText = "<nobr><a href='#'>" + Bundle.CPUSettingsBasicPanel_EditFilterString() + "</a></nobr>"; //NOI18N
        labelFocusedText = "<nobr><a href='#' color=\"" + colorText + "\">" + Bundle.CPUSettingsBasicPanel_EditFilterString() + "</a></nobr>"; //NOI18N
        editFilterLink = new HyperlinkLabel(labelText, labelFocusedText,
                                            new Runnable() {
                public void run() {
                    performQuickFilterAction();
                }
            });
        editFilterLink.setVisible(false);
        editFilterLink.setToolTipText(Bundle.StpEditFilterTooltip());
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 10);
        filterLinksContainer.add(editFilterLink, constraints);

        // editFilterSetsLink
        labelText = "<nobr><a href='#'>" + Bundle.CPUSettingsBasicPanel_EditFilterSetString() + "</a></nobr>"; //NOI18N
        labelFocusedText = "<nobr><a href='#' color=\"" + colorText + "\">" + Bundle.CPUSettingsBasicPanel_EditFilterSetString() + "</a></nobr>"; //NOI18N
        editFilterSetsLink = new HyperlinkLabel(labelText, labelFocusedText,
                                                new Runnable() {
                public void run() {
                    performCustomizeFilterSetsAction();
                }
            });
        editFilterSetsLink.setToolTipText(Bundle.StpManageFilterSetsTooltip());
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        filterLinksContainer.add(editFilterSetsLink, constraints);

        // filterLinksContainer - customization
        filterLinksContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 5, 0, 0);
        add(filterLinksContainer, constraints);

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
        org.openide.awt.Mnemonics.setLocalizedText(profilingPointsCheckbox, Bundle.CPUSettingsBasicPanel_UsePpsCheckboxText());
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
        labelText = "<nobr><a href='#'>" + Bundle.CPUSettingsBasicPanel_ShowPpsString() + "</a></nobr>"; //NOI18N
        labelFocusedText = "<nobr><a href='#' color=\"" + colorText + "\">" + Bundle.CPUSettingsBasicPanel_ShowPpsString() + "</a></nobr>"; //NOI18N
        profilingPointsLink = new HyperlinkLabel(labelText, labelFocusedText,
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

        // globalFiltersButton
        globalFiltersButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(globalFiltersButton, Bundle.CPUSettingsBasicPanel_EditGlobalFilterString());

        // UI tweaks
        Dimension d1 = showFilterLink.getPreferredSize();
        Dimension d2 = editFilterLink.getPreferredSize();
        Dimension d3 = new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
        showFilterLink.setPreferredSize(d3);
        editFilterLink.setPreferredSize(d3);
    }

    private void performCustomizeFilterSetsAction() {       
        final FilterSetsPanel filterSetsPanel = FilterSetsPanel.getDefault();

        final DialogDescriptor dd = new DialogDescriptor(filterSetsPanel, Bundle.CPUSettingsBasicPanel_FilterSetsDialogCaption(), true,
                                                         new Object[] { DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION },
                                                         DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN, null, null);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        dd.setAdditionalOptions(new Object[] { globalFiltersButton });
        dd.setButtonListener(this);

        int itemsToSkip = 4; // Exclude Java Core Classes and QuickFilter are above Custom FilterSets + 2 separators

        if (preferredInstrFilters != null) {
            itemsToSkip += preferredInstrFilters.size(); // Preferred instrumentation filter are above Custom FilterSets
        }

        filterSetsPanel.init(Math.max(filterCombo.getSelectedIndex() - itemsToSkip, 0));

        d.pack(); // allows correct resizing of textarea in FilterSetsPanel
        d.setVisible(true);
        dd.setButtonListener(null);

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            filterSetsPanel.applyChanges();

            if (filterSetsPanel.getSelectedFilterSet() != null) {
                selectedInstrumentationFilter = filterSetsPanel.getSelectedFilterSet();

                //        ProfilerIDESettings.getInstance().setInstrFilterDefault(filterComboBox.getSelectedItem().toString());
            }

            updateFilterComboBoxItems();
        }
    }

    private static void performEditGlobalFiltersAction() {
        final GlobalFiltersPanel globalFiltersPanel = GlobalFiltersPanel.getDefault();

        final DialogDescriptor dd = new DialogDescriptor(globalFiltersPanel, Bundle.CPUSettingsBasicPanel_GlobalFiltersDialogCaption(), true,
                                                         new Object[] {
                                                             globalFiltersPanel.getOKButton(),
                                                             globalFiltersPanel.getCancelButton()
                                                         }, globalFiltersPanel.getOKButton(), DialogDescriptor.BOTTOM_ALIGN,
                                                         null, null);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        globalFiltersPanel.init();
        d.pack(); // allows correct resizing of textarea in GlobalFiltersPanel
        d.setVisible(true);

        if (dd.getValue() == globalFiltersPanel.getOKButton()) {
            globalFiltersPanel.applyChanges();
            FilterSetsPanel.getDefault().processGlobalFiltersChanged();
        }
    }

    private void performQuickFilterAction() {
        QuickFilterPanel quickFilterPanel = QuickFilterPanel.getDefault();

        DialogDescriptor dd = new DialogDescriptor(quickFilterPanel, Bundle.CPUSettingsBasicPanel_QuickFilterDialogCaption(), true,
                                                   new Object[] { quickFilterPanel.getOKButton(), quickFilterPanel.getCancelButton() },
                                                   quickFilterPanel.getOKButton(), DialogDescriptor.BOTTOM_ALIGN, null, null);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        quickFilterPanel.init(quickFilter);

        d.pack(); // allows correct resizing of textarea in QuickFilter
        d.setVisible(true);

        if (dd.getValue() == quickFilterPanel.getOKButton()) {
            quickFilterPanel.applyChanges();
            filterCombo.setSelectedItem(quickFilter); // required from performProjectFilterAction()
        }

        updateControls();
        filterCombo.requestFocus();
    }

    final private AtomicBoolean rootMethodsActionExecuting = new AtomicBoolean(false);
    @NbBundle.Messages({
        "MSG_DefaultRoots=Computing default project profiling roots"
    })
    private void performRootMethodsAction() {
        if (rootMethodsActionExecuting.compareAndSet(false, true)) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    new SwingWorker(false) {
                        private ProgressDisplayer pd = ProfilerProgressDisplayer.getDefault();
                        final private AtomicBoolean cancelled = new AtomicBoolean(false);
                        private ClientUtils.SourceCodeSelection[] rms;

                        @Override
                        protected void doInBackground() {
                            if (rootMethods.length == 0) {
                                rms = ProjectContentsSupport.get(project).getProfilingRoots(null, ProjectUtilities.hasSubprojects(project));
                            } else {
                                rms = rootMethods;
                            }
                        }

                        @Override
                        protected void nonResponding() {
                            pd.showProgress(Bundle.MSG_DefaultRoots(), new ProgressDisplayer.ProgressController() {
                                @Override
                                public boolean cancel() {
                                    cancelled.set(true);
                                    return true;
                                }
                            });
                        }

                        @Override
                        protected void done() {
                            pd.close();
                            if (!cancelled.get()) {
                                ClientUtils.SourceCodeSelection[] roots = ProfilingRoots.selectRoots(rms, project);
                                if (roots != null) {
                                    rootMethods = roots;
                                    SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            updateControls();
                                        }
                                    });
                                }
                            }
                            rootMethodsActionExecuting.set(false);
                        }

                    }.execute();
                }
            });
        }
    }

    private void performShowFilterAction() {
        selectedInstrumentationFilter = filterCombo.getSelectedItem();

        if (!preferredInstrFilters.contains(selectedInstrumentationFilter)) {
            return; // TODO: implement for all filters
        }

        PreferredInstrFilterPanel preferredInstrFilterPanel = PreferredInstrFilterPanel.getDefault();
        PreferredInstrFilterPanel.FilterResolver filterComputer = new PreferredInstrFilterPanel.FilterResolver() {
            protected String[] computeFilterValues() {
                SimpleFilter sf = SelectProfilingTask.getDefault()
                                                     .getResolvedPredefinedFilter((SimpleFilter) selectedInstrumentationFilter);
                String[] fvs = FilterUtils.getSeparateFilters(sf.getFilterValue());

                return fvs;
            }
        };

        final DialogDescriptor dd = new DialogDescriptor(preferredInstrFilterPanel, 
                                                         Bundle.CPUSettingsBasicPanel_ShowFilterCaption(
                                                            ((SimpleFilter) selectedInstrumentationFilter).getFilterName()), 
                                                         true,
                                                         new Object[] {
                                                             preferredInstrFilterPanel.OPEN_IN_QUICKFILTER_BUTTON,
                                                             preferredInstrFilterPanel.CLOSE_BUTTON
                                                         }, preferredInstrFilterPanel.CLOSE_BUTTON,
                                                         DialogDescriptor.BOTTOM_ALIGN, null, null);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        preferredInstrFilterPanel.init(filterComputer);
        d.pack(); // allows correct resizing of textarea in PreferredInstrFilterPanel
        d.setVisible(true);

        if (dd.getValue() == preferredInstrFilterPanel.OPEN_IN_QUICKFILTER_BUTTON) {
            String[] filterValues = filterComputer.getFilterValues();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < filterValues.length; i++) {
                sb.append(filterValues[i]).append((i == (filterValues.length - 1)) ? "" : " "); //NOI18N
            }

            quickFilter.setFilterType(SimpleFilter.SIMPLE_FILTER_INCLUSIVE);
            quickFilter.setFilterValue(sb.toString());
            performQuickFilterAction();

            return;
        }

        filterCombo.requestFocus();
    }

    private void performShowProfilingPointsAction() {
        profilingPointsDisplayer.run();
    }

    private void selectActiveComboItem() {
        // Preselect first preferred instr. filter if any, otherwise preselect Profile All Classes
        int itemToSelect = (((preferredInstrFilters != null) && !preferredInstrFilters.isEmpty()) ? 1 : 0);

        // Try to override preselected item by current selection
        for (int i = 0; i < filterCombo.getItemCount(); i++) {
            Object comboItem = filterCombo.getItemAt(i);

            if (comboItem.equals(selectedInstrumentationFilter)) {
                itemToSelect = i;

                break;
            }
        }

        filterCombo.setSelectedIndex(itemToSelect);

        updateComboLinks();
    }

    private void updateComboLinks() {
        selectedInstrumentationFilter = filterCombo.getSelectedItem();
        if (selectedInstrumentationFilter == null) selectedInstrumentationFilter = SimpleFilter.NO_FILTER;

        if (quickFilter.equals(selectedInstrumentationFilter)) {
            showFilterLink.setVisible(false);
            editFilterLink.setVisible(true);
        } else if ((preferredInstrFilters != null) && preferredInstrFilters.contains(selectedInstrumentationFilter)) {
            showFilterLink.setEnabled(true);
            showFilterLink.setVisible(true);
            editFilterLink.setVisible(false);
        } else {
            showFilterLink.setEnabled(false);
            showFilterLink.setVisible(true);
            editFilterLink.setVisible(false);
        }
    }

    private void updateControls() {
        // TODO: cleanup!!!
        partOfAppHintLink.setVisible(profileAppRadio.isSelected());
        stopwatchHintLink.setVisible(stopwatchRadio.isSelected());

        boolean rootMethodsSubmitOK = true;
        boolean quickFilterSubmitOK = true;

        if (profileAppRadio.isSelected()) {
            Color linkColor = Color.RED;
            String colorText = "rgb(" + linkColor.getRed() + "," + linkColor.getGreen() + "," + linkColor.getBlue() + ")"; //NOI18N
            String textColorText = "rgb(" + Color.GRAY.getRed() + "," + Color.GRAY.getGreen() + "," + Color.GRAY.getBlue() + ")"; //NOI18N
            
            String labelText;
            String labelFocusedText;
            String labelToolTip;

            if (rootMethods.length == 0) {
                labelText = "<nobr>" + Bundle.CPUSettingsBasicPanel_DefaultRootsString(textColorText, "") + "</nobr>"; //NOI18N
                labelFocusedText = "<nobr>" //NOI18N
                                   + Bundle.CPUSettingsBasicPanel_DefaultRootsString(textColorText, "color=\"" + colorText + "\"") //NOI18N
                                   + "</nobr>"; //NOI18N
                labelToolTip = Bundle.CPUSettingsBasicPanel_DefaultRootsToolTip();
                rootMethodsSubmitOK = true;
            } else {
                labelText = "<nobr>" //NOI18N
                            + Bundle.CPUSettingsBasicPanel_CustomRootsString(textColorText, "") //NOI18N
                            + "</nobr>"; //NOI18N
                labelFocusedText = "<nobr>" //NOI18N
                                   + Bundle.CPUSettingsBasicPanel_CustomRootsString(textColorText, "color=\"" + colorText + "\"") + "</nobr>"; //NOI18N
                labelToolTip = Bundle.CPUSettingsBasicPanel_CustomRootsToolTip();
                rootMethodsSubmitOK = true;
            }

            partOfAppHintLink.setText(labelText, labelFocusedText);
            partOfAppHintLink.setToolTipText(labelToolTip);
        }

        if (quickFilter != null && quickFilter.equals(filterCombo.getSelectedItem())
                && (quickFilter.getFilterValue().trim().length() == 0)) {
            quickFilterSubmitOK = false;
        }

        if (rootMethodsSubmitOK && quickFilterSubmitOK) {
            SelectProfilingTask.getDefault().enableSubmitButton();
        } else {
            SelectProfilingTask.getDefault().disableSubmitButton();
        }

        if (project == null) {
            // TODO: processing for Attach - External Application
        } else if (sampleAppRadio.isSelected() || stopwatchRadio.isSelected()) {
            if (lastProfilingPointsState) {
                return;
            }

            profilingPointsStateCache = profilingPointsCheckbox.isSelected();
            profilingPointsCheckbox.setEnabled(false);
            profilingPointsCheckbox.setSelected(false);
            if (stopwatchRadio.isSelected()) {
                filterLabel.setEnabled(false);
                filterCombo.setEnabled(false);
                showFilterLink.setEnabled(false);
                editFilterSetsLink.setEnabled(false);
            }
            lastProfilingPointsState = true;
        } else {
            if (!lastProfilingPointsState) {
                return;
            }

            profilingPointsCheckbox.setEnabled(true);
            profilingPointsCheckbox.setSelected(profilingPointsStateCache);
            filterLabel.setEnabled(true);
            filterCombo.setEnabled(true);
            showFilterLink.setEnabled(true);
            editFilterSetsLink.setEnabled(true);
            lastProfilingPointsState = false;
        }
    }
    
    private void updateEnabling() {
        profilingPointsLink.setEnabled(profilingPointsCheckbox.isSelected() && profilingPointsCheckbox.isEnabled());
    }

    // --- Private implementation ------------------------------------------------
    private void updateFilterComboBoxItems() {
        filterCombo.removeActionListener(this);

        filterCombo.removeAllItems();

        // Profile all classes (stable)
        filterCombo.addItem(FilterUtils.NONE_FILTER);

        // filters defined by ProjectTypeProfiler
        if ((preferredInstrFilters != null) && (preferredInstrFilters.size() > 0)) {
            for (int i = 0; i < preferredInstrFilters.size(); i++) {
                filterCombo.addItem(preferredInstrFilters.get(i));
            }
        }

        // Separator
        filterCombo.addItem(new JSeparator());

        // Quick Filter (stable)
        filterCombo.addItem(quickFilter);

        // defined filterSets
        DefinedFilterSets definedFilterSets = Profiler.getDefault().getDefinedFilterSets();

        for (int i = 0; i < definedFilterSets.getFilterSetsCount(); i++) {
            if (i == 0) filterCombo.addItem(new JSeparator()); // Separator
            filterCombo.addItem(definedFilterSets.getFilterSetAt(i));
        }

        selectActiveComboItem();

        filterCombo.addActionListener(this);
    }
}
