/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.UIUtil.LayerItemPresenter;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.action.DataModel.Position;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * The second panel in the <em>New Action Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class GUIRegistrationPanel extends BasicWizardIterator.Panel {
    
    private final RequestProcessor SFS_RP = new RequestProcessor(GUIRegistrationPanel.class.getName());
    private FileSystem sfs;
    
    private static final ListCellRenderer POSITION_RENDERER = new PositionRenderer();
    private static final String POSITION_HERE =
            NbBundle.getMessage(GUIRegistrationPanel.class, "CTL_PositionHere");
    private static final String POSITION_SEPARATOR = " - "; // NOI18N
    
    private static final String WAIT_VALUE =
            NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_PleaseWait");
    private static final DefaultComboBoxModel WAIT_MODEL = new DefaultComboBoxModel(new Object[] { WAIT_VALUE });
    
    private static final String EMPTY_VALUE =
            NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Empty");
    private static final DefaultComboBoxModel EMPTY_MODEL = new DefaultComboBoxModel(new Object[] { EMPTY_VALUE });
    
    private DataModel data;
    private boolean firstTime = true;
    
    private final JComponent[] gmiGroup;
    private final JComponent[] toolbarGroup;
    private final JComponent[] shortcutGroup;
    private final JComponent[] fileTypeGroup;
    private final JComponent[] editorGroup;
    
    // XXX should be cleared when SFS of the module's universe has changed
    private static Map cachedPositionModels = new HashMap();
    
    public GUIRegistrationPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
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
            keyStroke, keyStrokeTxt, keyStrokeChange
        };
        fileTypeGroup = new JComponent[] {
            ftContentType, ftContentTypeTxt, ftPosition, ftPositionTxt, ftSeparatorAfter, ftSeparatorBefore
        };
        editorGroup = new JComponent[] {
            edContentType, edContentTypeTxt, edPosition, edPositionTxt, edSeparatorAfter, edSeparatorBefore
        };
        readSFS();
    }
    
    protected String getPanelName() {
        return getMessage("LBL_GUIRegistration_Title"); // NOI18N
    }
    
    protected void storeToDataModel() {
        // XXX this is just a prevention for the case when the user press Back button - should be ensured by a wizard (issue 63142)
        if (!checkValidity()) {
            return;
        }
        // second panel data (GUI Registration)
        data.setCategory(getSelectedLayerItem(category).getFullPath());
        // global menu item
        data.setGlobalMenuItemEnabled(globalMenuItem.isSelected());
        if (globalMenuItem.isSelected()) {
            data.setGMIParentMenu(getSelectedLayerItem(menu).getFullPath());
            data.setGMIPosition((Position) menuPosition.getSelectedItem());
            data.setGMISeparatorAfter(menuSeparatorAfter.isSelected());
            data.setGMISeparatorBefore(menuSeparatorBefore.isSelected());
        }
        // global toolbar button
        data.setToolbarEnabled(globalToolbarButton.isSelected());
        if (globalToolbarButton.isSelected()) {
            data.setToolbar(getSelectedLayerItem(toolbar).getFullPath());
            data.setToolbarPosition((Position) toolbarPosition.getSelectedItem());
        }
        // global keyboard shortcut
        data.setKeyboardShortcutEnabled(globalKeyboardShortcut.isSelected());
        // file type context menu item
        data.setFileTypeContextEnabled(fileTypeContext.isSelected());
        if (fileTypeContext.isSelected()) {
            data.setFTContextType(getSelectedLayerItem(ftContentType).getFullPath());
            data.setFTContextPosition((Position) ftPosition.getSelectedItem());
            data.setFTContextSeparatorBefore(ftSeparatorBefore.isSelected());
            data.setFTContextSeparatorAfter(ftSeparatorAfter.isSelected());
        }
        // editor context menu item
        data.setEditorContextEnabled(editorContext.isSelected());
        if (editorContext.isSelected()) {
            data.setEdContextType(getSelectedLayerItem(edContentType).getFullPath());
            data.setEdContextPosition((Position) edPosition.getSelectedItem());
            data.setEdContextSeparatorBefore(edSeparatorBefore.isSelected());
            data.setEdContextSeparatorAfter(edSeparatorAfter.isSelected());
        }
    }
    
    protected void readFromDataModel() {
        initializeGlobalAction();
        if (firstTime) {
            firstTime = false;
            setValid(Boolean.FALSE);
        } else {
            checkValidity();
        }
    }
    
    private void initializeGlobalAction() {
        globalMenuItem.setSelected(true);
        
        globalMenuItem.setEnabled(true);
        setGroupEnabled(gmiGroup, globalMenuItem.isSelected());
        
        globalToolbarButton.setEnabled(true);
        setGroupEnabled(toolbarGroup, globalToolbarButton.isSelected());
        
        boolean alwaysEnabled = data.isAlwaysEnabled();
        globalKeyboardShortcut.setEnabled(alwaysEnabled);
        setGroupEnabled(shortcutGroup, globalKeyboardShortcut.isSelected());
        
        if (alwaysEnabled) {
            fileTypeContext.setSelected(false);
            editorContext.setSelected(false);
        }
        fileTypeContext.setEnabled(!alwaysEnabled);
        setGroupEnabled(fileTypeGroup, fileTypeContext.isSelected());
        
        editorContext.setEnabled(!alwaysEnabled);
        setGroupEnabled(editorGroup, editorContext.isSelected());
    }
    
    private boolean checkValidity() {
        boolean result = false;
        if (globalKeyboardShortcut.isSelected() && keyStroke.getText().equals("")) { // NOI18N
            setErrorMessage(getMessage("MSG_YouMustSpecifyShortcut")); // NOI18N
        } else if (!check(globalMenuItem, menu, menuPosition) ||
                !check(globalToolbarButton, toolbar, toolbarPosition) ||
                !check(fileTypeContext, ftContentType, ftPosition) ||
                !check(editorContext, edContentType, edPosition)) {
            setValid(Boolean.FALSE);
        } else {
            setErrorMessage(null);
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
                group[i].setEnabled(enabled
                        && !(group[i] instanceof JComboBox && ((JComboBox) group[i]).getModel() == EMPTY_MODEL));
            }
        }
    }
    
    private void readSFS() {
        setValid(Boolean.FALSE);
        loadCombo("Actions", category); // NOI18N
        loadComboAndPositions("Menu", menu, menuPosition, null); // NOI18N
        loadComboAndPositions("Toolbars", toolbar, toolbarPosition, null); // NOI18N
        loadComboAndPositions("Loaders/text", ftContentType, ftPosition, "Actions"); // NOI18N
        loadComboAndPositions("Editors/text", edContentType, edPosition, "Popup"); // NOI18N
    }
    
    private void loadCombo(final String startFolder,
            final JComboBox combo) {
        loadComboAndPositions(startFolder, combo, null, null);
    }
    
    private void loadComboAndPositions(final String startFolder,
            final JComboBox combo,
            final JComboBox comboPositions,
            final String subFolder) {
        combo.setModel(WAIT_MODEL);
        SFS_RP.post(new Runnable() {
            public void run() {
                Util.err.log("Loading " + startFolder + " from SFS...."); // NOI18N
                final FileObject parent = getSFS().getRoot().getFileObject(startFolder);
                if (parent == null) {
                    Util.err.log(ErrorManager.WARNING, "Could not find " + startFolder); // NOI18N
                    setEmptyModel(combo);
                    setEmptyModel(comboPositions);
                    return;
                }
                final Enumeration items = subFolder == null
                        ? parent.getFolders(true) : loadFolders(parent, subFolder);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        // sort items
                        Collection sorted = new TreeSet();
                        while (items.hasMoreElements()) {
                            FileObject item = (FileObject) items.nextElement();
                            sorted.add(new LayerItemPresenter(item, parent, subFolder != null));
                        }
                        // create model
                        DefaultComboBoxModel model = new DefaultComboBoxModel();
                        for (Iterator it = sorted.iterator(); it.hasNext(); ) {
                            model.addElement(it.next());
                        }
                        combo.setModel(model);
                        // load positions combo
                        if (comboPositions != null) {
                            loadPositionsCombo((LayerItemPresenter) combo.getSelectedItem(),
                                    comboPositions);
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
        ComboBoxModel model = (ComboBoxModel) cachedPositionModels.get(parent);
        if (model == null) {
            positionsCombo.setModel(WAIT_MODEL);
            SFS_RP.post(new Runnable() {
                public void run() {
                    final Enumeration filesEn = parent.getFileObject().getData(false);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            createPositionModel(positionsCombo, filesEn, parent);
                        }
                    });
                }
            });
        } else {
            positionsCombo.setModel(model);
            checkValidity();
        }
    }
    
    private void createPositionModel(final JComboBox positionsCombo,
            final Enumeration filesEn,
            final LayerItemPresenter parent) {
        DefaultComboBoxModel newModel = new DefaultComboBoxModel();
        LayerItemPresenter previous = null;
        while (filesEn.hasMoreElements()) {
            LayerItemPresenter current = new LayerItemPresenter(
                    (FileObject) filesEn.nextElement(),
                    parent.getFileObject());
            newModel.addElement(createPosition(previous, current));
            previous = current;
        }
        newModel.addElement(createPosition(previous, null));
        cachedPositionModels.put(parent, newModel);
        positionsCombo.setModel(newModel);
        checkValidity();
    }
    
    private static Object getSelectedItem(JComboBox combo) {
        Object item = combo.getSelectedItem();
        return (item == WAIT_VALUE || item == EMPTY_VALUE) ? null : item;
    }
    
    private static LayerItemPresenter getSelectedLayerItem(JComboBox combo) {
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
            combo.setModel(EMPTY_MODEL);
            combo.setEnabled(false);
            checkValidity();
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
        keyStroke = new javax.swing.JLabel();
        keyStrokeChange = new javax.swing.JButton();
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
        org.openide.awt.Mnemonics.setLocalizedText(categoryTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Category"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
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

        org.openide.awt.Mnemonics.setLocalizedText(globalMenuItem, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_GlobalMenuItem"));
        globalMenuItem.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        globalMenuItem.setMargin(new java.awt.Insets(0, 0, 0, 0));
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

        menuTxt.setLabelFor(menu);
        org.openide.awt.Mnemonics.setLocalizedText(menuTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Menu"));
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

        menuPositionTxt.setLabelFor(menuPosition);
        org.openide.awt.Mnemonics.setLocalizedText(menuPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position"));
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

        menuSeparatorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(menuSeparatorBefore, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorBefore"));
        menuSeparatorBefore.setMargin(new java.awt.Insets(0, 0, 0, 0));
        menuSeparatorPanel.add(menuSeparatorBefore);

        org.openide.awt.Mnemonics.setLocalizedText(menuSeparatorAfter, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorAfter"));
        menuSeparatorAfter.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 6, 0, 0)));
        menuSeparatorAfter.setMargin(new java.awt.Insets(0, 0, 0, 0));
        menuSeparatorPanel.add(menuSeparatorAfter);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(menuSeparatorPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(globalToolbarButton, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_GlobalToolbarButton"));
        globalToolbarButton.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        globalToolbarButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
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

        toolbarTxt.setLabelFor(toolbar);
        org.openide.awt.Mnemonics.setLocalizedText(toolbarTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Toolbar"));
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

        toolbarPositionTxt.setLabelFor(toolbarPosition);
        org.openide.awt.Mnemonics.setLocalizedText(toolbarPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position"));
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

        org.openide.awt.Mnemonics.setLocalizedText(globalKeyboardShortcut, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_GlobalKeyboardShortcut"));
        globalKeyboardShortcut.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        globalKeyboardShortcut.setMargin(new java.awt.Insets(0, 0, 0, 0));
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

        keyStrokeTxt.setLabelFor(menuPosition);
        org.openide.awt.Mnemonics.setLocalizedText(keyStrokeTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_KeyStroke"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(keyStrokeTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(keyStroke, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(keyStrokeChange, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "CTL_Change"));
        keyStrokeChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyStrokeChangeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(keyStrokeChange, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fileTypeContext, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_FileTypeContextMenuItem"));
        fileTypeContext.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        fileTypeContext.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeContextActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        add(fileTypeContext, gridBagConstraints);

        ftContentTypeTxt.setLabelFor(ftContentType);
        org.openide.awt.Mnemonics.setLocalizedText(ftContentTypeTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_ContentType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(ftContentTypeTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(ftContentType, gridBagConstraints);

        ftPositionTxt.setLabelFor(ftPosition);
        org.openide.awt.Mnemonics.setLocalizedText(ftPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(ftPositionTxt, gridBagConstraints);

        ftPosition.setRenderer(POSITION_RENDERER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(ftPosition, gridBagConstraints);

        ftSeparatorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(ftSeparatorBefore, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorBefore"));
        ftSeparatorBefore.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ftSeparatorPanel.add(ftSeparatorBefore);

        org.openide.awt.Mnemonics.setLocalizedText(ftSeparatorAfter, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorAfter"));
        ftSeparatorAfter.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 6, 0, 0)));
        ftSeparatorAfter.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ftSeparatorPanel.add(ftSeparatorAfter);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(ftSeparatorPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editorContext, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_EditorContextMenuItem"));
        editorContext.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        editorContext.setMargin(new java.awt.Insets(0, 0, 0, 0));
        editorContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editorContextActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        add(editorContext, gridBagConstraints);

        edContentTypeTxt.setLabelFor(ftContentType);
        org.openide.awt.Mnemonics.setLocalizedText(edContentTypeTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_ContentType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(edContentTypeTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(edContentType, gridBagConstraints);

        edPositionTxt.setLabelFor(ftPosition);
        org.openide.awt.Mnemonics.setLocalizedText(edPositionTxt, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_Position"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 0);
        add(edPositionTxt, gridBagConstraints);

        edPosition.setRenderer(POSITION_RENDERER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 3, 0);
        add(edPosition, gridBagConstraints);

        edSeparatorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(edSeparatorBefore, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorBefore"));
        edSeparatorBefore.setMargin(new java.awt.Insets(0, 0, 0, 0));
        edSeparatorPanel.add(edSeparatorBefore);

        org.openide.awt.Mnemonics.setLocalizedText(edSeparatorAfter, org.openide.util.NbBundle.getMessage(GUIRegistrationPanel.class, "LBL_SeparatorAfter"));
        edSeparatorAfter.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 6, 0, 0)));
        edSeparatorAfter.setMargin(new java.awt.Insets(0, 0, 0, 0));
        edSeparatorPanel.add(edSeparatorAfter);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(edSeparatorPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
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
        setGroupEnabled(shortcutGroup, globalKeyboardShortcut.isSelected());
        checkValidity();
    }//GEN-LAST:event_globalKeyboardShortcutActionPerformed
    
    private void keyStrokeChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyStrokeChangeActionPerformed
        ShortcutEnterPanel sepPanel = new ShortcutEnterPanel();
        DialogDescriptor dd = new DialogDescriptor(sepPanel, getMessage("LBL_AddShortcutTitle")); // NOI18N
        Dialog addshort = DialogDisplayer.getDefault().createDialog(dd);
        addshort.setVisible(true);
        if (dd.getValue().equals(DialogDescriptor.OK_OPTION)) {
            keyStroke.setText(sepPanel.getKeyText());
            data.setKeyStroke(Utilities.keyToString(sepPanel.getKeyStroke()));
            checkValidity();
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
    private javax.swing.JCheckBox editorContext;
    private javax.swing.JCheckBox fileTypeContext;
    private javax.swing.JLabel filler;
    private javax.swing.JComboBox ftContentType;
    private javax.swing.JLabel ftContentTypeTxt;
    private javax.swing.JComboBox ftPosition;
    private javax.swing.JLabel ftPositionTxt;
    private javax.swing.JCheckBox ftSeparatorAfter;
    private javax.swing.JCheckBox ftSeparatorBefore;
    private javax.swing.JPanel ftSeparatorPanel;
    private javax.swing.JCheckBox globalKeyboardShortcut;
    private javax.swing.JCheckBox globalMenuItem;
    private javax.swing.JCheckBox globalToolbarButton;
    private javax.swing.JLabel keyStroke;
    private javax.swing.JButton keyStrokeChange;
    private javax.swing.JLabel keyStrokeTxt;
    private javax.swing.JComboBox menu;
    private javax.swing.JComboBox menuPosition;
    private javax.swing.JLabel menuPositionTxt;
    private javax.swing.JCheckBox menuSeparatorAfter;
    private javax.swing.JCheckBox menuSeparatorBefore;
    private javax.swing.JPanel menuSeparatorPanel;
    private javax.swing.JLabel menuTxt;
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
    
    Enumeration loadFolders(final FileObject startFolder, final String subFolder) {
        Enumeration en = startFolder.getFolders(true);
        Vector included = new Vector(); // let's be in sync with o.o.filesystems
        while (en.hasMoreElements()) {
            FileObject fo = (FileObject) en.nextElement();
            if (subFolder.equals(fo.getName())) {
                included.add(fo);
            }
        }
        return included.elements();
    }
    
    private static class PositionRenderer extends DefaultListCellRenderer {
        
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text;
            if (value == null || value == WAIT_VALUE) {
                text = WAIT_VALUE;
            } else if (value == EMPTY_VALUE) {
                text = EMPTY_VALUE;
            } else {
                Position pos = (Position) value;
                String before = pos.getBeforeName() == null ? "" : pos.getBeforeName() + POSITION_SEPARATOR;
                String after = pos.getAfterName() == null ? "" : POSITION_SEPARATOR + pos.getAfterName();
                text = before + POSITION_HERE + after;
            }
            Component c = super.getListCellRendererComponent(
                    list, text, index, isSelected, cellHasFocus);
            return c;
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
            LayerItemPresenter item = getSelectedLayerItem(menu);
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
