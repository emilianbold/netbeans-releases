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
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.ui.components.JExtendedRadioButton;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "NewCustomConfiguration_MonitorString=&Monitor",
    "NewCustomConfiguration_CpuString=&CPU",
    "NewCustomConfiguration_MemoryString=M&emory",
    "NewCustomConfiguration_NewConfigDialogCaption=New Custom Configuration",
    "NewCustomConfiguration_DuplicateConfigDialogCaption=Duplicate Configuration ({0})",
    "NewCustomConfiguration_RenameConfigDialogCaption=Rename Configuration ({0})",
    "NewCustomConfiguration_NewConfigString=New Configuration",
    "NewCustomConfiguration_NewMonitorString=New Monitoring",
    "NewCustomConfiguration_NewCpuString=New CPU Analysis",
    "NewCustomConfiguration_NewMemoryString=New Memory Analysis",
    "NewCustomConfiguration_TypeLabelText=Type:",
    "NewCustomConfiguration_NameLabelText=&Name:",
    "NewCustomConfiguration_NameLabelAccessDescr=Name of the custom configuration",
    "NewCustomConfiguration_InitSettingsLabelText=Initial Settings:",
    "NewCustomConfiguration_DefaultRadioText=&Default",
    "NewCustomConfiguration_DefaultRadioAccessDescr=Use default settings for the configuration",
    "NewCustomConfiguration_ExistingRadioText=&From Existing Configuration:",
    "NewCustomConfiguration_ExistingRadioAccessDescr=Copy settings from existing configuration",
    "NewCustomConfiguration_OkButtonText=OK"
})
public class NewCustomConfiguration extends JPanel implements ChangeListener, ListSelectionListener, DocumentListener, HelpCtx.Provider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    private static final String HELP_CTX_KEY_NEW = "NewCustomConfiguration.HelpCtx"; // NOI18N
    private static final String HELP_CTX_KEY_DUPLICATE = "DuplicateConfiguration.HelpCtx"; // NOI18N
    private static final String HELP_CTX_KEY_RENAME = "RenameConfiguration.HelpCtx"; // NOI18N

    // --- Constants declaration -------------------------------------------------
    private static final int MODE_NEW_ANY = 0;
    private static final int MODE_NEW_TYPE = 1;
    private static final int MODE_DUPLICATE = 2;
    private static final int MODE_RENAME = 4;

    // --- Instance variables declaration ----------------------------------------
    private static NewCustomConfiguration defaultInstance;

    // --- UI components declaration ---------------------------------------------
    private static final Icon ICON_MONITOR = Icons.getIcon(ProfilerIcons.MONITORING);
    private static final Icon ICON_CPU = Icons.getIcon(ProfilerIcons.CPU);
    private static final Icon ICON_MEMORY = Icons.getIcon(ProfilerIcons.MEMORY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private DefaultListModel existingSettingsListModel;
    private JButton okButton;
    private JLabel nameLabel;
    private JLabel settingsLabel;
    private JLabel typeLabel;
    private JList existingSettingsList;
    private JPanel bottomRenameSpacer;
    private JRadioButton cpuTypeRadio;
    private JRadioButton defaultSettingsRadio;
    private JRadioButton existingSettingsRadio;
    private JRadioButton memoryTypeRadio;
    private JRadioButton monitorTypeRadio;
    private JScrollPane existingSettingsScrollPane;
    private JTextField nameTextfield;
    private ProfilingSettings originalSettings = null;
    private ProfilingSettings[] availableSettings;
    private int mode;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Private implementation ------------------------------------------------
    private NewCustomConfiguration() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static ProfilingSettings createDuplicateConfiguration(ProfilingSettings originalConfiguration,
                                                                 ProfilingSettings[] availableConfigurations) {
        NewCustomConfiguration ncc = getDefault();
        ncc.setupDuplicateConfiguration(originalConfiguration, availableConfigurations);

        final DialogDescriptor dd = new DialogDescriptor(ncc,
                                                         Bundle.NewCustomConfiguration_DuplicateConfigDialogCaption(originalConfiguration.getSettingsName()));
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        d.setVisible(true);

        ProfilingSettings newSettings = null;

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            newSettings = ncc.getProfilingSettings();
        }

        return newSettings;
    }

    // --- Public interface ------------------------------------------------------
    public static ProfilingSettings createNewConfiguration(ProfilingSettings[] availableConfigurations) {
        NewCustomConfiguration ncc = getDefault();
        ncc.setupUniversalConfiguration(availableConfigurations);

        final DialogDescriptor dd = new DialogDescriptor(ncc, Bundle.NewCustomConfiguration_NewConfigDialogCaption());
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        d.setVisible(true);

        ProfilingSettings newSettings = null;

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            newSettings = ncc.getProfilingSettings();
        }

        return newSettings;
    }

    public static ProfilingSettings createNewConfiguration(int type, ProfilingSettings[] availableConfigurations) { // Use ProfilingSettings.getProfilingType() value

        NewCustomConfiguration ncc = getDefault();
        ncc.setupTypeConfiguration(type, availableConfigurations);

        String typeString = ""; // NOI18N

        if (ProfilingSettings.isMonitorSettings(type)) {
            typeString = " (" + Bundle.NewCustomConfiguration_MonitorString() + ")"; // NOI18N
        } else if (ProfilingSettings.isCPUSettings(type)) {
            typeString = " (" + Bundle.NewCustomConfiguration_CpuString() + ")"; // NOI18N
        } else if (ProfilingSettings.isMemorySettings(type)) {
            typeString = " (" + Bundle.NewCustomConfiguration_MemoryString() + ")"; // NOI18N
        }
        
        // Remove mnemonics wildcard
        typeString = typeString.replace("&", ""); // NOI18N

        final DialogDescriptor dd = new DialogDescriptor(ncc, Bundle.NewCustomConfiguration_NewConfigDialogCaption() + typeString, true,
                                                         new Object[] { ncc.okButton, DialogDescriptor.CANCEL_OPTION },
                                                         ncc.okButton, 0, null, null);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        d.setVisible(true);

        ProfilingSettings newSettings = null;

        if (dd.getValue() == ncc.okButton) {
            newSettings = ncc.getProfilingSettings();
        }

        return newSettings;
    }

    public static ProfilingSettings renameConfiguration(ProfilingSettings originalConfiguration,
                                                        ProfilingSettings[] availableConfigurations) {
        NewCustomConfiguration ncc = getDefault();
        ncc.setupRenameConfiguration(originalConfiguration, availableConfigurations);

        final DialogDescriptor dd = new DialogDescriptor(ncc,
                                                         Bundle.NewCustomConfiguration_RenameConfigDialogCaption(
                                                            originalConfiguration.getSettingsName()));
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        d.setVisible(true);

        ProfilingSettings newSettings = null;

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            newSettings = ncc.getProfilingSettings();
        }

        return newSettings;
    }
    
    public HelpCtx getHelpCtx() {
        switch (mode) {
            case MODE_NEW_ANY:
            case MODE_NEW_TYPE:
                return new HelpCtx(HELP_CTX_KEY_NEW);
            case MODE_DUPLICATE:
                return new HelpCtx(HELP_CTX_KEY_DUPLICATE);
            case MODE_RENAME:
                return new HelpCtx(HELP_CTX_KEY_RENAME);
            default:
                return null;
        }
    }

    public void changedUpdate(DocumentEvent e) {
        updateOKButton();
    }

    public void insertUpdate(DocumentEvent e) {
        updateOKButton();
    }

    // --- Static tester frame ---------------------------------------------------

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //NOI18N
            //      UIManager.setLookAndFeel("plaf.metal.MetalLookAndFeel"); //NOI18N
            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); //NOI18N
            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //NOI18N
        } catch (Exception e) {
        }

        ;

        //    NewCustomConfiguration.getDefault().createNewConfiguration();

        //    JFrame frame = new JFrame("Tester Frame");
        //    JPanel contents = new NewCustomConfiguration();
        ////    contents.setPreferredSize(new Dimension(375, 255));
        //    frame.getContentPane().add(contents);
        //    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //    frame.pack();
        //    frame.setVisible(true);
    }

    public void removeUpdate(DocumentEvent e) {
        updateOKButton();
    }

    // --- ChangeListener & ListSelectionListener & DocumentListner implementation
    public void stateChanged(ChangeEvent e) {
        existingSettingsList.setEnabled(existingSettingsRadio.isSelected());

        if (existingSettingsRadio.isEnabled() && defaultSettingsRadio.isSelected()) {
            existingSettingsList.clearSelection();
        }

        updateOKButton();
    }

    public void valueChanged(ListSelectionEvent e) {
        updateOKButton();
    }

    private static NewCustomConfiguration getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new NewCustomConfiguration();
        }

        return defaultInstance;
    }

    private ProfilingSettings getProfilingSettings() {
        ProfilingSettings newSettings = null;

        if (mode == MODE_RENAME) {
            // rename settings
            newSettings = originalSettings;
        } else if ((mode == MODE_DUPLICATE)
                       || (((mode == MODE_NEW_ANY) || (mode == MODE_NEW_TYPE)) && existingSettingsRadio.isSelected())) {
            // duplicate settings (new based on existing or duplicate)
            newSettings = new ProfilingSettings();
            availableSettings[existingSettingsList.getSelectedIndex()].copySettingsInto(newSettings);
        } else {
            // new default settings
            if (monitorTypeRadio.isSelected()) {
                newSettings = ProfilingSettingsPresets.createMonitorPreset();
                newSettings.setIsPreset(false);
            } else if (cpuTypeRadio.isSelected()) {
                newSettings = ProfilingSettingsPresets.createCPUPreset();
                newSettings.setIsPreset(false);
            } else if (memoryTypeRadio.isSelected()) {
                newSettings = ProfilingSettingsPresets.createMemoryPreset();
                newSettings.setIsPreset(false);
            }
        }

        newSettings.setSettingsName(nameTextfield.getText().trim());

        return newSettings;
    }

    private String createSettingsName(ProfilingSettings[] availableConfigurations) {
        String nameBasis = Bundle.NewCustomConfiguration_NewConfigString();

        if (monitorTypeRadio.isSelected()) {
            nameBasis = Bundle.NewCustomConfiguration_NewMonitorString();
        } else if (cpuTypeRadio.isSelected()) {
            nameBasis = Bundle.NewCustomConfiguration_NewCpuString();
        } else if (memoryTypeRadio.isSelected()) {
            nameBasis = Bundle.NewCustomConfiguration_NewMemoryString();
        }

        List<String> configurationsNames = new ArrayList(availableConfigurations.length);

        for (ProfilingSettings settings : availableConfigurations) {
            configurationsNames.add(settings.getSettingsName());
        }

        int index = 0;
        String indexStr = ""; // NOI18N

        while (configurationsNames.contains(nameBasis + indexStr)) {
            indexStr = " " + Integer.toString(++index); // NOI18N
        }

        return nameBasis + indexStr;
    }
    
    private void showTypeSettings() {
        typeLabel.setVisible(true);
        monitorTypeRadio.setVisible(true);
        cpuTypeRadio.setVisible(true);
        memoryTypeRadio.setVisible(true);
    }
    
    private void hideTypeSettings() {
        typeLabel.setVisible(false);
        monitorTypeRadio.setVisible(false);
        cpuTypeRadio.setVisible(false);
        memoryTypeRadio.setVisible(false);
    }

    // --- UI definition ---------------------------------------------------------
    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;
        ButtonGroup typeRadiosGroup = new ButtonGroup();
        ButtonGroup settingsRadiosGroup = new ButtonGroup();

        // typeLabel
        typeLabel = new JLabel(Bundle.NewCustomConfiguration_TypeLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 10, 0, 0);
        add(typeLabel, constraints);

        // monitorTypeRadio
        monitorTypeRadio = new JExtendedRadioButton(Bundle.NewCustomConfiguration_MonitorString(), ICON_MONITOR);
        org.openide.awt.Mnemonics.setLocalizedText(monitorTypeRadio, Bundle.NewCustomConfiguration_MonitorString());
        typeRadiosGroup.add(monitorTypeRadio);
        monitorTypeRadio.setSelected(true);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 5, 0, 0);
        add(monitorTypeRadio, constraints);

        // cpuTypeRadio
        cpuTypeRadio = new JExtendedRadioButton(Bundle.NewCustomConfiguration_CpuString(), ICON_CPU);
        org.openide.awt.Mnemonics.setLocalizedText(cpuTypeRadio, Bundle.NewCustomConfiguration_CpuString());
        typeRadiosGroup.add(cpuTypeRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 5, 0, 0);
        add(cpuTypeRadio, constraints);

        // memoryTypeRadio
        memoryTypeRadio = new JExtendedRadioButton(Bundle.NewCustomConfiguration_MemoryString(), ICON_MEMORY);
        org.openide.awt.Mnemonics.setLocalizedText(memoryTypeRadio, Bundle.NewCustomConfiguration_MemoryString());
        typeRadiosGroup.add(memoryTypeRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 5, 0, 10);
        add(memoryTypeRadio, constraints);

        // nameLabel
        nameLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, Bundle.NewCustomConfiguration_NameLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 0, 0);
        add(nameLabel, constraints);

        // nameTextfield
        nameTextfield = new JTextField();
        nameTextfield.getDocument().addDocumentListener(this);
        nameTextfield.setPreferredSize(new Dimension(250, nameTextfield.getPreferredSize().height));
        nameTextfield.getAccessibleContext().setAccessibleDescription(Bundle.NewCustomConfiguration_NameLabelAccessDescr());
        nameLabel.setLabelFor(nameTextfield);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 5, 0, 10);
        add(nameTextfield, constraints);

        // settingsLabel
        settingsLabel = new JLabel(Bundle.NewCustomConfiguration_InitSettingsLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 0, 10);
        add(settingsLabel, constraints);

        // defaultSettingsRadio
        defaultSettingsRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(defaultSettingsRadio, Bundle.NewCustomConfiguration_DefaultRadioText());
        settingsRadiosGroup.add(defaultSettingsRadio);
        defaultSettingsRadio.getAccessibleContext().setAccessibleDescription(Bundle.NewCustomConfiguration_DefaultRadioAccessDescr());
        defaultSettingsRadio.addChangeListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 0, 10);
        add(defaultSettingsRadio, constraints);

        // existingSettingsRadio
        existingSettingsRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(existingSettingsRadio, Bundle.NewCustomConfiguration_ExistingRadioText());
        settingsRadiosGroup.add(existingSettingsRadio);
        existingSettingsRadio.getAccessibleContext().setAccessibleDescription(Bundle.NewCustomConfiguration_ExistingRadioAccessDescr());
        existingSettingsRadio.setSelected(true);
        existingSettingsRadio.addChangeListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 5, 0, 10);
        add(existingSettingsRadio, constraints);

        // existingSettingsList
        existingSettingsListModel = new DefaultListModel();
        existingSettingsList = new JList(existingSettingsListModel);
        existingSettingsList.setVisibleRowCount(5);
        existingSettingsList.addListSelectionListener(this);

        // existingSettingsScrollPane
        existingSettingsScrollPane = new JScrollPane(existingSettingsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(4, 5, 15, 10);
        add(existingSettingsScrollPane, constraints);

        // bottomRenameSpacer
        bottomRenameSpacer = UIUtils.createFillerPanel();
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 15, 0);
        add(bottomRenameSpacer, constraints);

        // okButton
        okButton = new JButton(Bundle.NewCustomConfiguration_OkButtonText());

        // UI tweaks
        addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && isShowing()) {
                        nameTextfield.requestFocusInWindow();
                        nameTextfield.selectAll();
                    }
                }
            });
    }

    private void setupDuplicateConfiguration(ProfilingSettings originalConfiguration, ProfilingSettings[] availableConfigurations) {
        mode = MODE_DUPLICATE;
        originalSettings = originalConfiguration;
        availableSettings = availableConfigurations;

        monitorTypeRadio.setEnabled(ProfilingSettings.isMonitorSettings(originalSettings));
        monitorTypeRadio.setSelected(monitorTypeRadio.isEnabled());
        cpuTypeRadio.setEnabled(ProfilingSettings.isCPUSettings(originalSettings));
        cpuTypeRadio.setSelected(cpuTypeRadio.isEnabled());
        memoryTypeRadio.setEnabled(ProfilingSettings.isMemorySettings(originalSettings));
        memoryTypeRadio.setSelected(memoryTypeRadio.isEnabled());
        hideTypeSettings();

        settingsLabel.setVisible(true);
        defaultSettingsRadio.setVisible(true);
        existingSettingsRadio.setVisible(true);
        existingSettingsScrollPane.setVisible(true);
        bottomRenameSpacer.setVisible(false);

        defaultSettingsRadio.setEnabled(false);
        existingSettingsRadio.setEnabled(false);
        existingSettingsRadio.setSelected(true);
        existingSettingsList.setEnabled(false);

        nameTextfield.setText(createSettingsName(availableConfigurations));
        updateAvailableSettings();

        for (int i = 0; i < availableConfigurations.length; i++) {
            if (originalSettings == availableConfigurations[i]) {
                existingSettingsList.setSelectedIndex(i);

                return;
            }
        }

        updateOKButton();
    }

    private void setupRenameConfiguration(ProfilingSettings originalConfiguration, ProfilingSettings[] availableConfigurations) {
        mode = MODE_RENAME;
        originalSettings = originalConfiguration;
        availableSettings = availableConfigurations;

        monitorTypeRadio.setEnabled(ProfilingSettings.isMonitorSettings(originalSettings));
        monitorTypeRadio.setSelected(monitorTypeRadio.isEnabled());
        cpuTypeRadio.setEnabled(ProfilingSettings.isCPUSettings(originalSettings));
        cpuTypeRadio.setSelected(cpuTypeRadio.isEnabled());
        memoryTypeRadio.setEnabled(ProfilingSettings.isMemorySettings(originalSettings));
        memoryTypeRadio.setSelected(memoryTypeRadio.isEnabled());
        hideTypeSettings();

        settingsLabel.setVisible(false);
        defaultSettingsRadio.setVisible(false);
        existingSettingsRadio.setVisible(false);
        existingSettingsScrollPane.setVisible(false);
        bottomRenameSpacer.setVisible(true);

        nameTextfield.setText(originalConfiguration.getSettingsName());
        updateAvailableSettings();
        updateOKButton();
    }

    private void setupTypeConfiguration(int type, ProfilingSettings[] availableConfigurations) {
        mode = MODE_NEW_TYPE;
        originalSettings = null;
        availableSettings = availableConfigurations;

        monitorTypeRadio.setEnabled(ProfilingSettings.isMonitorSettings(type));
        monitorTypeRadio.setSelected(monitorTypeRadio.isEnabled());
        cpuTypeRadio.setEnabled(ProfilingSettings.isCPUSettings(type));
        cpuTypeRadio.setSelected(cpuTypeRadio.isEnabled());
        memoryTypeRadio.setEnabled(ProfilingSettings.isMemorySettings(type));
        memoryTypeRadio.setSelected(memoryTypeRadio.isEnabled());
        hideTypeSettings();

        settingsLabel.setVisible(true);
        defaultSettingsRadio.setVisible(true);
        existingSettingsRadio.setVisible(true);
        existingSettingsScrollPane.setVisible(true);
        bottomRenameSpacer.setVisible(false);

        defaultSettingsRadio.setEnabled(true);
        defaultSettingsRadio.setSelected(true);
        existingSettingsRadio.setEnabled(true);

        nameTextfield.setText(createSettingsName(availableConfigurations));
        updateAvailableSettings();
        updateOKButton();
    }

    private void setupUniversalConfiguration(ProfilingSettings[] availableConfigurations) {
        mode = MODE_NEW_ANY;
        originalSettings = null;
        availableSettings = availableConfigurations;

        monitorTypeRadio.setEnabled(true);
        cpuTypeRadio.setEnabled(true);
        cpuTypeRadio.setSelected(true);
        memoryTypeRadio.setEnabled(true);
        showTypeSettings();

        settingsLabel.setVisible(true);
        defaultSettingsRadio.setVisible(true);
        existingSettingsRadio.setVisible(true);
        existingSettingsScrollPane.setVisible(true);
        bottomRenameSpacer.setVisible(false);

        defaultSettingsRadio.setEnabled(true);
        defaultSettingsRadio.setSelected(true);
        existingSettingsRadio.setEnabled(true);

        nameTextfield.setText(createSettingsName(availableConfigurations));
        updateAvailableSettings();
        updateOKButton();
    }

    private void updateAvailableSettings() {
        existingSettingsListModel.removeAllElements();

        for (ProfilingSettings settings : availableSettings) {
            existingSettingsListModel.addElement(settings.getSettingsName());
        }
    }

    private void updateOKButton() {
        okButton.setEnabled((nameTextfield.getText().trim().length() > 0)
                            && (defaultSettingsRadio.isSelected() || (existingSettingsList.getSelectedIndex() != -1)));
    }
}
