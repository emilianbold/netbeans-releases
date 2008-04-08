/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.UIUtil.LayerItemPresenter;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.action.DataModel.Position;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The second panel in the <em>New Action Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class GUIRegistrationPanel extends BasicWizardIterator.Panel {
    
    private final RequestProcessor SFS_RP = new RequestProcessor(GUIRegistrationPanel.class.getName());
    private static final String ACTIONS_DIR = "Actions"; // NOI18N
    
    private FileSystem sfs;
    
    private final ListCellRenderer POSITION_RENDERER = new PositionRenderer();
    private static final String POSITION_HERE = getMessage("CTL_PositionHere");
    private static final String POSITION_SEPARATOR = " - "; // NOI18N
    
    private DataModel data;
    
    private final JComponent[] gmiGroup;
    private final JComponent[] toolbarGroup;
    private final JComponent[] shortcutGroup;
    private final JComponent[] fileTypeGroup;
    private final JComponent[] editorGroup;
    
    public GUIRegistrationPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        putClientProperty("NewFileWizard_Title", getMessage("LBL_ActionWizardTitle"));
        
        menu.addPopupMenuListener(new PML(menu, menuPosition));
        toolbar.addPopupMenuListener(new PML(toolbar, toolbarPosition));
        ftContentType.addPopupMenuListener(new PML(ftContentType, ftPosition));
        edContentType.addPopupMenuListener(new PML(edContentType, edPosition));
        gmiGroup = new JComponent[] {
            menu, menuTxt, menuPosition, menuPositionTxt, menuSeparatorAfter, menuSeparatorBefore
        };
        toolbarGroup = new JComponent[] {
            toolbar, toolbarTxt, toolbarPosition, toolbarPositionTxt
        };
        shortcutGroup = new JComponent[] {
            shortcutsList, keyStrokeTxt, keyStrokeChange, keyStrokeRemove
        };
        fileTypeGroup = new JComponent[] {
            ftContentType, ftContentTypeTxt, ftPosition, ftPositionTxt, ftSeparatorAfter, ftSeparatorBefore
        };
        editorGroup = new JComponent[] {
            edContentType, edContentTypeTxt, edPosition, edPositionTxt, edSeparatorAfter, edSeparatorBefore
        };
        readSFS();
    }
    
    private void setEditable(final JComboBox combo) {
        combo.setEditable(true);
        if (combo.getEditor().getEditorComponent() instanceof JTextField) {
            JTextField txt = (JTextField) combo.getEditor().getEditorComponent();
            // XXX check if there are not multiple (--> redundant) listeners
            txt.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
                public void insertUpdate(DocumentEvent e) {
                    if (!CustomizerComponentFactory.isWaitModel(combo.getModel())) {
                        checkValidity();
                    }
                }
            });
        }
    }
    
    protected String getPanelName() {
        return getMessage("LBL_GUIRegistration_Title");
    }
    
    private String getCategoryPath() {
        String path = UIUtil.getSFSPath(category, ACTIONS_DIR);
        return path == null ? ACTIONS_DIR + '/' + "Tools" : path; // NOI18N
    }
    
    protected void storeToDataModel() {
        // XXX this is just a prevention for the case when the user press Back button - should be ensured by a wizard (issue 63142)
        if (!checkValidity()) {
            return;
        }
        // second panel data (GUI Registration)
        data.setCategory(getCategoryPath());
        // global menu item
        data.setGlobalMenuItemEnabled(globalMenuItem.isSelected());
        if (globalMenuItem.isSelected()) {
            data.setGMIParentMenu(getSelectedLayerPresenter(menu).getFullPath());
            data.setGMIPosition((Position) menuPosition.getSelectedItem());
            data.setGMISeparatorAfter(menuSeparatorAfter.isSelected());
            data.setGMISeparatorBefore(menuSeparatorBefore.isSelected());
        }
        // global toolbar button
        data.setToolbarEnabled(globalToolbarButton.isSelected());
        if (globalToolbarButton.isSelected()) {
            data.setToolbar(getSelectedLayerPresenter(toolbar).getFullPath());
            data.setToolbarPosition((Position) toolbarPosition.getSelectedItem());
        }
        // global keyboard shortcut
        data.setKeyboardShortcutEnabled(globalKeyboardShortcut.isSelected());
        // file type context menu item
        data.setFileTypeContextEnabled(fileTypeContext.isSelected());
        if (fileTypeContext.isSelected()) {
            data.setFTContextType(getSelectedLayerPresenter(ftContentType).getFullPath());
            data.setFTContextPosition((Position) ftPosition.getSelectedItem());
            data.setFTContextSeparatorBefore(ftSeparatorBefore.isSelected());
            data.setFTContextSeparatorAfter(ftSeparatorAfter.isSelected());
        }
        // editor context menu item
        data.setEditorContextEnabled(editorContext.isSelected());
        if (editorContext.isSelected()) {
            data.setEdContextType(getSelectedLayerPresenter(edContentType).getFullPath());
            data.setEdContextPosition((Position) edPosition.getSelectedItem());
            data.setEdContextSeparatorBefore(edSeparatorBefore.isSelected());
            data.setEdContextSeparatorAfter(edSeparatorAfter.isSelected());
        }
    }
    
    protected void readFromDataModel() {
        initializeGlobalAction();
        checkValidity();
    }
    
    private void initializeGlobalAction() {
        globalMenuItem.setSelected(true);
        
        globalMenuItem.setEnabled(true);
        setGroupEnabled(gmiGroup, globalMenuItem.isSelected());
        
        globalToolbarButton.setEnabled(true);
        setGroupEnabled(toolbarGroup, globalToolbarButton.isSelected());
        
        boolean alwaysEnabled = data.isAlwaysEnabled();
        globalKeyboardShortcut.setEnabled(alwaysEnabled);
        setShortcutGroupEnabled();
        
        if (alwaysEnabled) {
            fileTypeContext.setSelected(false);
            editorContext.setSelected(false);
        }
        fileTypeContext.setEnabled(!alwaysEnabled);
        setGroupEnabled(fileTypeGroup, fileTypeContext.isSelected());
        
        editorContext.setEnabled(!alwaysEnabled);
        setGroupEnabled(editorGroup, editorContext.isSelected());
    }
    
    /** Package private for unit tests only. */
    boolean checkValidity() {
        boolean result = false;
        if (globalKeyboardShortcut.isSelected() && ((DefaultListModel)shortcutsList.getModel()).isEmpty()) { // NOI18N
            setError(getMessage("MSG_YouMustSpecifyShortcut"));
        } else if (!check(globalMenuItem, menu, menuPosition) ||
                !check(globalToolbarButton, toolbar, toolbarPosition) ||
                !check(fileTypeContext, ftContentType, ftPosition) ||
                !check(editorContext, edContentType, edPosition)) {
            markInvalid();
        } else if (!Util.isValidSFSPath(getCategoryPath())) {
            setError(getMessage("ERR_Category_Invalid"));
        } else {
            markValid();
            result = true;
        }
        return result;
    }
    
    private boolean check(JCheckBox groupCheckBox, JComboBox menu, JComboBox position) {
        boolean result = !groupCheckBox.isSelected() ||
                (getSelectedItem(menu) != null && getSelectedItem(position) != null);
        return result;
    }
    
    private void setGroupEnabled(JComponent[] group, boolean enabled) {
        for (int i = 0; i < group.length; i++) {
            if (group[i] != null) {
                group[i].setEnabled(enabled && !isEmptyCombo(group[i]));
            }
        }
    }

    private void setShortcutGroupEnabled() {
        boolean isEnabled = globalKeyboardShortcut.isSelected();        
        setGroupEnabled(shortcutGroup, isEnabled);
        isEnabled = isEnabled && shortcutsList.getSelectedValues().length > 0;
        keyStrokeRemove.setEnabled(isEnabled);
    }

    private boolean isEmptyCombo(JComponent c) {
        return c instanceof JComboBox &&
                CustomizerComponentFactory.hasOnlyValue(((JComboBox) c).getModel(), CustomizerComponentFactory.EMPTY_VALUE);
    }
    
    private void readSFS() {
        markInvalid();
        loadComboAndPositions(ACTIONS_DIR, category, null, null, true);
        loadComboAndPositions("Menu", menu, menuPosition, null); // NOI18N
        loadComboAndPositions("Toolbars", toolbar, toolbarPosition, null); // NOI18N
        loadComboAndPositions("Loaders", ftContentType, ftPosition, "Actions"); // NOI18N
        loadComboAndPositions("Editors", edContentType, edPosition, "Popup"); // NOI18N
    }
    
    private void loadComboAndPositions(final String startFolder,
            final JComboBox combo,
            final JComboBox comboPositions,
            final String subFolderName) {
        loadComboAndPositions(startFolder, combo, comboPositions, subFolderName, false);
    }
    
    /** See {@link #getFoldersByName(DataFolder, String)} for subFolderName explanation . */
    private void loadComboAndPositions(final String startFolder,
            final JComboBox combo,
            final JComboBox comboPositions,
            final String subFolderName,
            final boolean editable) {
        combo.setModel(CustomizerComponentFactory.createComboWaitModel());
        SFS_RP.post(new Runnable() {
            public void run() {
                Util.err.log("Loading " + startFolder + " from SFS...."); // NOI18N
                final FileObject parent = getSFS().getRoot().getFileObject(startFolder);
                final DataFolder parentDF = (parent != null ? DataFolder.findFolder(parent) : null);
                if (parentDF == null) {
                    Util.err.log("Could not find " + startFolder);
                    setEmptyModel(combo);
                    setEmptyModel(comboPositions);
                    return;
                }
                final List<DataFolder> folders = subFolderName == null
                        ? getFolders(parentDF) : getFoldersByName(parentDF, subFolderName);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Collection<LayerItemPresenter> sorted = new LinkedHashSet<LayerItemPresenter>();
                        for (DataFolder folder : folders) {
                            sorted.add(new LayerItemPresenter(folder.getPrimaryFile(), parent, subFolderName != null));
                        }
                        if (sorted.size() == 0) {
                            setEmptyModel(combo);
                            setEmptyModel(comboPositions);
                        } else {
                            // create model
                            DefaultComboBoxModel model = new DefaultComboBoxModel();
                            for (Iterator it = sorted.iterator(); it.hasNext(); ) {
                                model.addElement(it.next());
                            }
                            combo.setModel(model);
                            if (editable) {
                                setEditable(combo);
                            }
                            // load positions combo
                            if (comboPositions != null) {
                                loadPositionsCombo((LayerItemPresenter) combo.getSelectedItem(),
                                        comboPositions);
                            }
                        }
                    }
                });
            }
        });
    }

    private void loadPositionsCombo(
            final LayerItemPresenter parent,
            final JComboBox positionsCombo) {
        
        assert parent != null;
        assert positionsCombo != null;
        positionsCombo.setModel(CustomizerComponentFactory.createComboWaitModel());
        SFS_RP.post(new Runnable() {
            public void run() {
                DataObject[] kids = DataFolder.findFolder(parent.getFileObject()).getChildren(); // #71820: sort!
                final FileObject[] files = new FileObject[kids.length];
                for (int i = 0; i < kids.length; i++) {
                    files[i] = kids[i].getPrimaryFile();
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        createPositionModel(positionsCombo, files, parent);
                    }
                });
            }
        });
    }
    
    private void createPositionModel(final JComboBox positionsCombo,
            final FileObject[] files,
            final LayerItemPresenter parent) {
        DefaultComboBoxModel newModel = new DefaultComboBoxModel();
        LayerItemPresenter previous = null;
        for (int i = 0; i < files.length; i++) {
            LayerItemPresenter current = new LayerItemPresenter(
                    files[i],
                    parent.getFileObject());
            newModel.addElement(createPosition(previous, current));
            previous = current;
        }
        newModel.addElement(createPosition(previous, null));
        positionsCombo.setModel(newModel);
        checkValidity();
    }
   
    private static Object getSelectedItem(final JComboBox combo) {
        Object item = combo.getSelectedItem();
        return (item == CustomizerComponentFactory.WAIT_VALUE || item == CustomizerComponentFactory.EMPTY_VALUE) ? null : item;
    }
    
    private static LayerItemPresenter getSelectedLayerPresenter(final JComboBox combo) {
        return (LayerItemPresenter) getSelectedItem(combo);
    }
    
    private static Position createPosition(LayerItemPresenter first, LayerItemPresenter second) {
        return new Position(
                first == null ? null : first.getFileObject().getNameExt(),
                second == null ? null : second.getFileObject().getNameExt(),
                first == null ? null : first.getDisplayName(),
                second == null ? null : second.getDisplayName());
    }
    
    private void setEmptyModel(JComboBox combo) {
        if (combo != null) {
            combo.setModel(CustomizerComponentFactory.createComboEmptyModel());
            combo.setEnabled(false);
            combo.setEditable(false);
            checkValidity();
        }
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(GUIRegistrationPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(GUIRegistrationPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        categoryTxt = new javax.swing.JLabel();
        category = new javax.swing.JComboBox();
        globalMenuItem = new javax.swing.JCheckBox();
        menuTxt = new javax.swing.JLabel();
        menu = new javax.swing.JComboBox();
        menuPositionTxt = new javax.swing.JLabel();
        menuPosition = new javax.swing.JComboBox();
        menuSeparatorPanel = new javax.swing.JPanel();
        menuSeparatorBefore = new javax.swing.JCheckBox();
        menuSeparatorAfter = new javax.swing.JCheckBox();
        globalToolbarButton = new javax.swing.JCheckBox();
        toolbarTxt = new javax.swing.JLabel();
        toolbar = new javax.swing.JComboBox();
        toolbarPositionTxt = new javax.swing.JLabel();
        toolbarPosition = new javax.swing.JComboBox();
        globalKeyboardShortcut = new javax.swing.JCheckBox();
        keyStrokeTxt = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        shortcutsList = new JList(new DefaultListModel());
        keyStrokeChange = new javax.swing.JButton();
        keyStrokeRemove = new javax.swing.JButton();
        filler = new javax.swing.JLabel();
        fileTypeContext = new javax.swing.JCheckBox();
        ftContentTypeTxt = new javax.swing.JLabel();
        ftContentType = new javax.swing.JComboBox();
        ftPositionTxt = new javax.swing.JLabel();
        ftPosition = new javax.swing.JComboBox();
        ftSeparatorPanel = new javax.swing.JPanel();
        ftSeparatorBefore = new javax.swing.JCheckBox();
        ftSeparatorAfter = new javax.swing.JCheckBox();
        editorContext = new javax.swing.JCheckBox();
        edContentTypeTxt = new javax.swing.JLabel();
        edContentType = new javax.swing.JComboBox();
        edPositionTxt = new javax.swing.JLabel();
        edPosition = new javax.swing.JComboBox();
        edSeparatorPanel = new javax.swing.JPanel();
        edSeparatorBefore = new javax.swing.JCheckBox();
        edSeparatorAfter = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        categoryTxt.setLabelFor(category);
        org.openide.awt.Mnemonics.setLocalizedText(categoryTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Category")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(categoryTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(category, gridBagConstraints);
        category.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_Category")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(globalMenuItem, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_GlobalMenuItem")); // NOI18N
        globalMenuItem.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        globalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalMenuItemActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        add(globalMenuItem, gridBagConstraints);
        globalMenuItem.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_globalMenuItem")); // NOI18N

        menuTxt.setLabelFor(menu);
        org.openide.awt.Mnemonics.setLocalizedText(menuTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Menu")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(menuTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(menu, gridBagConstraints);
        menu.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_menu")); // NOI18N

        menuPositionTxt.setLabelFor(menuPosition);
        org.openide.awt.Mnemonics.setLocalizedText(menuPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(menuPositionTxt, gridBagConstraints);

        menuPosition.setRenderer(POSITION_RENDERER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(menuPosition, gridBagConstraints);
        menuPosition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_menuPosition")); // NOI18N

        menuSeparatorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(menuSeparatorBefore, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorBefore")); // NOI18N
        menuSeparatorPanel.add(menuSeparatorBefore);
        menuSeparatorBefore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_menuSeparatorBefore")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(menuSeparatorAfter, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorAfter")); // NOI18N
        menuSeparatorAfter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        menuSeparatorPanel.add(menuSeparatorAfter);
        menuSeparatorAfter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_menuSeparatorAfter")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(menuSeparatorPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(globalToolbarButton, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_GlobalToolbarButton")); // NOI18N
        globalToolbarButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        globalToolbarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalToolbarButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        add(globalToolbarButton, gridBagConstraints);
        globalToolbarButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_globalToolbarButton")); // NOI18N

        toolbarTxt.setLabelFor(toolbar);
        org.openide.awt.Mnemonics.setLocalizedText(toolbarTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Toolbar")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(toolbarTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(toolbar, gridBagConstraints);
        toolbar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_toolbar")); // NOI18N

        toolbarPositionTxt.setLabelFor(toolbarPosition);
        org.openide.awt.Mnemonics.setLocalizedText(toolbarPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(toolbarPositionTxt, gridBagConstraints);

        toolbarPosition.setRenderer(POSITION_RENDERER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(toolbarPosition, gridBagConstraints);
        toolbarPosition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_toolbarPosition")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(globalKeyboardShortcut, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_GlobalKeyboardShortcut")); // NOI18N
        globalKeyboardShortcut.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        globalKeyboardShortcut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalKeyboardShortcutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        add(globalKeyboardShortcut, gridBagConstraints);
        globalKeyboardShortcut.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_globalKeyboardShortcut")); // NOI18N

        keyStrokeTxt.setLabelFor(menuPosition);
        org.openide.awt.Mnemonics.setLocalizedText(keyStrokeTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_KeyStroke")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(keyStrokeTxt, gridBagConstraints);
        keyStrokeTxt.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_keyStrokeDef")); // NOI18N

        shortcutsList.setVisibleRowCount(3);
        shortcutsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                shortcutsListChanged(evt);
            }
        });
        jScrollPane1.setViewportView(shortcutsList);
        shortcutsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_keyStrokeList")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(keyStrokeChange, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "CTL_Change")); // NOI18N
        keyStrokeChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyStrokeChangeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(keyStrokeChange, gridBagConstraints);
        keyStrokeChange.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_keyStrokeChange")); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/action/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(keyStrokeRemove, bundle.getString("CTL_Remove")); // NOI18N
        keyStrokeRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyStrokeRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(keyStrokeRemove, gridBagConstraints);
        keyStrokeRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_keyStrokeRemove")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fileTypeContext, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_FileTypeContextMenuItem")); // NOI18N
        fileTypeContext.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileTypeContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeContextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        add(fileTypeContext, gridBagConstraints);
        fileTypeContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_FileTypeContext")); // NOI18N

        ftContentTypeTxt.setLabelFor(ftContentType);
        org.openide.awt.Mnemonics.setLocalizedText(ftContentTypeTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_ContentType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(ftContentTypeTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(ftContentType, gridBagConstraints);
        ftContentType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_ftContentType")); // NOI18N

        ftPositionTxt.setLabelFor(ftPosition);
        org.openide.awt.Mnemonics.setLocalizedText(ftPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(ftPositionTxt, gridBagConstraints);

        ftPosition.setRenderer(POSITION_RENDERER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(ftPosition, gridBagConstraints);
        ftPosition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_ftPosition")); // NOI18N

        ftSeparatorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(ftSeparatorBefore, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorBefore")); // NOI18N
        ftSeparatorPanel.add(ftSeparatorBefore);
        ftSeparatorBefore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_ftSeparatorBefore")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ftSeparatorAfter, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorAfter")); // NOI18N
        ftSeparatorAfter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        ftSeparatorPanel.add(ftSeparatorAfter);
        ftSeparatorAfter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_ftSeparatorAfter")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(ftSeparatorPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editorContext, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_EditorContextMenuItem")); // NOI18N
        editorContext.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        editorContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editorContextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        add(editorContext, gridBagConstraints);
        editorContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_EditorContext")); // NOI18N

        edContentTypeTxt.setLabelFor(edContentType);
        org.openide.awt.Mnemonics.setLocalizedText(edContentTypeTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_ContentType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(edContentTypeTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(edContentType, gridBagConstraints);
        edContentType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_edContentType")); // NOI18N

        edPositionTxt.setLabelFor(edPosition);
        org.openide.awt.Mnemonics.setLocalizedText(edPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(edPositionTxt, gridBagConstraints);

        edPosition.setRenderer(POSITION_RENDERER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(edPosition, gridBagConstraints);
        edPosition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_edPosition")); // NOI18N

        edSeparatorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(edSeparatorBefore, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorBefore")); // NOI18N
        edSeparatorPanel.add(edSeparatorBefore);
        edSeparatorBefore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_edSeparatorBefore")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(edSeparatorAfter, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorAfter")); // NOI18N
        edSeparatorAfter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        edSeparatorPanel.add(edSeparatorAfter);
        edSeparatorAfter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_CTL_edSeparatorAfter")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(edSeparatorPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "ACS_GuiRegistrationPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void shortcutsListChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_shortcutsListChanged
        setShortcutGroupEnabled();
    }//GEN-LAST:event_shortcutsListChanged

    private void keyStrokeRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyStrokeRemoveActionPerformed
        DefaultListModel lm = (DefaultListModel)shortcutsList.getModel();
        Object[] selected = shortcutsList.getSelectedValues();
        if (selected.length > 0) {
            int idx = shortcutsList.getSelectionModel().getMinSelectionIndex();
            for (int i = 0; i < selected.length; i++) {
                lm.removeElement(selected[i]);
            }
            if (lm.getSize() > 0) {
                idx = (idx > 0) ? idx -1 : 0;
               shortcutsList.setSelectedIndex(idx); 
            }
        }
        checkValidity();
    }//GEN-LAST:event_keyStrokeRemoveActionPerformed
    
    private void editorContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editorContextActionPerformed
        setGroupEnabled(editorGroup, editorContext.isSelected());
        checkValidity();
    }//GEN-LAST:event_editorContextActionPerformed
    
    private void fileTypeContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTypeContextActionPerformed
        setGroupEnabled(fileTypeGroup, fileTypeContext.isSelected());
        checkValidity();
    }//GEN-LAST:event_fileTypeContextActionPerformed
    
    private void globalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalMenuItemActionPerformed
        setGroupEnabled(gmiGroup, globalMenuItem.isSelected());
        
        checkValidity();
    }//GEN-LAST:event_globalMenuItemActionPerformed
    
    private void globalToolbarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalToolbarButtonActionPerformed
        setGroupEnabled(toolbarGroup, globalToolbarButton.isSelected());
        checkValidity();
    }//GEN-LAST:event_globalToolbarButtonActionPerformed
    
    private void globalKeyboardShortcutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalKeyboardShortcutActionPerformed
        setShortcutGroupEnabled();
        checkValidity();
    }//GEN-LAST:event_globalKeyboardShortcutActionPerformed
    
    private void keyStrokeChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyStrokeChangeActionPerformed
        KeyStroke[] keyStrokes = ShortcutEnterPanel.showDialog();
        if (keyStrokes != null && keyStrokes.length > 0) {
            String newShortcut = UIUtil.keyStrokesToString(keyStrokes);
            DefaultListModel lm = (DefaultListModel)shortcutsList.getModel();
            if (!lm.contains(newShortcut)) {
                lm.addElement(newShortcut);
                data.setKeyStroke(UIUtil.keyStrokesToLogicalString(keyStrokes));
                shortcutsList.setSelectedValue(newShortcut, true);
                checkValidity();
            }
        }        
    }//GEN-LAST:event_keyStrokeChangeActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox category;
    private javax.swing.JLabel categoryTxt;
    private javax.swing.JComboBox edContentType;
    private javax.swing.JLabel edContentTypeTxt;
    private javax.swing.JComboBox edPosition;
    private javax.swing.JLabel edPositionTxt;
    private javax.swing.JCheckBox edSeparatorAfter;
    private javax.swing.JCheckBox edSeparatorBefore;
    private javax.swing.JPanel edSeparatorPanel;
    javax.swing.JCheckBox editorContext;
    javax.swing.JCheckBox fileTypeContext;
    private javax.swing.JLabel filler;
    private javax.swing.JComboBox ftContentType;
    private javax.swing.JLabel ftContentTypeTxt;
    private javax.swing.JComboBox ftPosition;
    private javax.swing.JLabel ftPositionTxt;
    private javax.swing.JCheckBox ftSeparatorAfter;
    private javax.swing.JCheckBox ftSeparatorBefore;
    private javax.swing.JPanel ftSeparatorPanel;
    private javax.swing.JCheckBox globalKeyboardShortcut;
    javax.swing.JCheckBox globalMenuItem;
    javax.swing.JCheckBox globalToolbarButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton keyStrokeChange;
    private javax.swing.JButton keyStrokeRemove;
    private javax.swing.JLabel keyStrokeTxt;
    private javax.swing.JComboBox menu;
    private javax.swing.JComboBox menuPosition;
    private javax.swing.JLabel menuPositionTxt;
    private javax.swing.JCheckBox menuSeparatorAfter;
    private javax.swing.JCheckBox menuSeparatorBefore;
    private javax.swing.JPanel menuSeparatorPanel;
    private javax.swing.JLabel menuTxt;
    private javax.swing.JList shortcutsList;
    private javax.swing.JComboBox toolbar;
    private javax.swing.JComboBox toolbarPosition;
    private javax.swing.JLabel toolbarPositionTxt;
    private javax.swing.JLabel toolbarTxt;
    // End of variables declaration//GEN-END:variables
    
    /** Don't call me from EDT! */
    private FileSystem getSFS() {
        assert !EventQueue.isDispatchThread() : "Called from ETD!"; // NOI18N
        if (sfs == null) {
            try {
                // XXX takes very long time. Consider to call it when e.g. module is loaded
                sfs = LayerUtils.getEffectiveSystemFilesystem(data.getProject());
            } catch (IOException ex) {
                Util.err.notify(ex);
                sfs = FileUtil.createMemoryFileSystem();
            }
        }
        return sfs;
    }
    
    /**
     * Actually really dedicated for Loader&hellip;Actions and
     * Editors&hellip;Popup.
     */
    private List<DataFolder> getFoldersByName(final DataFolder startFolder, final String subFoldersName) {
        List<DataFolder> result = new ArrayList<DataFolder>();
        for (DataFolder dObj : getFolders(startFolder)) {
            if (subFoldersName.equals(dObj.getName()) &&
                    dObj.getPrimaryFile().getParent() != startFolder.getPrimaryFile()) {
                result.add(dObj);
            }
        }
        return result;
    }
    
    private static List<DataFolder> getFolders(DataFolder folder) {
        List<DataFolder> folders = new ArrayList<DataFolder>();
        getFolders(folder, folders); // #66144: depth-first, not breadth-first, search appropriate here
        return folders;
    }
    
    private static void getFolders(DataFolder folder, List<DataFolder> folders) {
        for (DataObject d : folder.getChildren()) {
            if (d instanceof DataFolder) {
                DataFolder f = (DataFolder) d;
                folders.add(f);
                getFolders(f, folders);
            }
        }
    }
    
    private static class PositionRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public PositionRenderer () {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String text;
            if (value == null || value == CustomizerComponentFactory.WAIT_VALUE) {
                text = CustomizerComponentFactory.WAIT_VALUE;
            } else if (value == CustomizerComponentFactory.EMPTY_VALUE) {
                text = CustomizerComponentFactory.EMPTY_VALUE;
            } else {
                Position pos = (Position) value;
                String before = pos.getBeforeName() == null ? "" : pos.getBeforeName() + POSITION_SEPARATOR;
                String after = pos.getAfterName() == null ? "" : POSITION_SEPARATOR + pos.getAfterName();
                text = before + POSITION_HERE + after;
            }
            setText(text);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }
    
    private class PML implements PopupMenuListener {
        
        private JComboBox menu;
        private JComboBox position;
        
        PML(JComboBox menu, JComboBox position) {
            this.menu = menu;
            this.position = position;
        }
        
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            LayerItemPresenter item = getSelectedLayerPresenter(menu);
            if (item != null) {
                loadPositionsCombo(item, position);
            }
        }
        
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            // we don't care
        }
        
        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenuWillBecomeInvisible(null);
        }
        
    }
    
}
