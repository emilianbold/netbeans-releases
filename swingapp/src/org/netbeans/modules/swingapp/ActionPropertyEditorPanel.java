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
package org.netbeans.modules.swingapp;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.editors.ClassPathFileChooser;
import org.netbeans.modules.form.editors.IconEditor;
import org.netbeans.modules.swingapp.ProxyAction.Scope;
import org.netbeans.modules.swingapp.actions.AcceleratorKeyListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * This is the actual dialog used for editing actions. It lets the user set
 * almost any attribute on an action. The action will not be saved from this
 * dialog, however. When the user presses the Okay button (defined in the NB supplied
 * property editor dialog) the ActionEditor class will do the actual saving.
 * @author  joshua.marinacci@sun.com
 */
public class ActionPropertyEditorPanel extends javax.swing.JPanel {
    
    public static final String LARGE_ICON_KEY = "SwingLargeIconKey"; // NOI18N
    
    private Map<ProxyAction.Scope, List<ProxyAction>> parsedActions;
    private boolean newActionCreated = false;
    private boolean actionPropertiesUpdated = false;
    private boolean returnsTask = false;
    private ProxyAction.Scope newActionScope = ProxyAction.Scope.Application;
    private boolean viewSource = false;
    private String newMethodName = "";
    private boolean isChanging = false;
    private Map<ProxyAction.Scope, String> scopeClasses = new HashMap<ProxyAction.Scope, String>();
    private FileObject sourceFile;
    private String smallIconName = null;
    private String largeIconName = null;
    private ProxyAction newAction = null;
    private ProxyAction globalAction = null;
    private boolean globalMode = false;
    private ProxyAction NEW_ACTION = new ProxyAction("-newaction-","-id-"); // NOI18N
    private FileObject selectedSourceFile;
    private AcceleratorKeyListener acceleratorListener;
    
    
    enum Mode { Form, NewActionForm, Global, NewActionGlobal}
    
    private Mode mode;
    
    /** Creates new form ActionPropertyEditorPanel */
    public ActionPropertyEditorPanel(final FormProperty property, FileObject sourceFile) {
        initComponents();
        this.sourceFile = sourceFile;
        if(property == null) {
            globalMode = true;
        }
        parsedActions = new HashMap<ProxyAction.Scope, List<ProxyAction>>();
        
        Object[] vals = new Object[] {
            ProxyAction.BlockingType.NONE,
            ProxyAction.BlockingType.ACTION,
            ProxyAction.BlockingType.COMPONENT,
            ProxyAction.BlockingType.WINDOW,
            ProxyAction.BlockingType.APPLICATION };
        blockingType.setModel(new DefaultComboBoxModel(vals));
        blockingType.setSelectedItem(ProxyAction.BlockingType.NONE);
        blockingType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionPropertiesUpdated = true;
            }
        });
        
        DocumentListener dirtyListener = new DirtyDocumentListener();
        textField.getDocument().addDocumentListener(dirtyListener);
        tooltipField.getDocument().addDocumentListener(dirtyListener);
        acceleratorText.getDocument().addDocumentListener(dirtyListener);
        
        this.addPropertyChangeListener("action", new PropertyChangeListener() { // NOI18N
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getNewValue() != null) {
                    ProxyAction act = (ProxyAction)evt.getNewValue();
                    if(!isNewActionCreated()) {
                        updateFieldsFromAction(act);
                    }
                } else {
                    clearFieldsForNull();
                }
            }
            
        });
        
        ((IconButton)iconButtonLarge).setIconText(NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.iconButtonLarge.text"));
        ((IconButton)iconButtonSmall).setIconText(NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.iconButtonSmall.text"));
        iconButtonSmall.addActionListener(new IconButtonListener(property,iconButtonSmall, Action.SMALL_ICON));
        iconButtonLarge.addActionListener(new IconButtonListener(property,iconButtonLarge, LARGE_ICON_KEY));
        setIconButtonSmall.addActionListener(new IconButtonListener(property,iconButtonSmall, Action.SMALL_ICON));
        setIconButtonLarge.addActionListener(new IconButtonListener(property,iconButtonLarge, LARGE_ICON_KEY));
        
        scopeCombo.setModel(new DefaultComboBoxModel(new ProxyAction.Scope[] { ProxyAction.Scope.Form, ProxyAction.Scope.Application}));
        
        scopeCombo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                ProxyAction.Scope scope = (ProxyAction.Scope)value;
                String className = scopeClasses.get(scope);
                if(className != null) {
                    ((JLabel)comp).setText(scope.toString() + ": " + className);
                } else {
                    ((JLabel)comp).setText(scope.toString());
                }
                return comp;
            }
        });
        
        actionsCombo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                String text = getLocalizedString("noneAction");
                if(value instanceof ProxyAction) {
                    ProxyAction act = (ProxyAction)value;
                    if(value == NEW_ACTION) {
                        text = getLocalizedString("createNewAction");
                    } else {
                        text = act != null ? act.getId() : getLocalizedString("noneAction");
                        if(act != null && act.isAppWide()) {
                            text += getLocalizedString("globalActionAppend");//" (global)";
                        }
                    }
                }
                ((JLabel)comp).setText(text);
                return comp;
            }
        });
        
        setupAccelField();
        if(globalMode) {
            this.setMode(Mode.Global);
        } else {
            this.setMode(Mode.Form);
        }
        
        enabledCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enabledTextfield.setText((String)enabledCombo.getSelectedItem());
            }
        });
        selectedCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedTextfield.setText((String)selectedCombo.getSelectedItem());
            }
        });
        
        updatePropertyCombos(null);
        
        ActionListener modifierListener = new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                acceleratorListener.updateFromModifiers();
            }
        };
        altCheckbox.addActionListener(modifierListener);
        controlCheckbox.addActionListener(modifierListener);
        metaCheckbox.addActionListener(modifierListener);
        shiftCheckbox.addActionListener(modifierListener);
    }
    
    /* update the selected and enabled combos based on the class of the action
     * passed in. If the action is null, then use this sourcefile provided to this
     * ActionPropertyEditorPanel. If that is null then the combos will be disabled.
     * The combos will also be disabled if there are no boolean properties.
     */
    private void updatePropertyCombos(ProxyAction act) {
        List<String> strings = new ArrayList<String>();
        if(act != null) {
            FileObject classFile = ActionManager.getActionManager(sourceFile).getFileForClass(act.getClassname());
            strings = ActionManager.findBooleanProperties(classFile);
        } else if(sourceFile != null) {
            strings = ActionManager.findBooleanProperties(sourceFile);
        }
        enabledCombo.setModel(new DefaultComboBoxModel(strings.toArray()));
        selectedCombo.setModel(new DefaultComboBoxModel(strings.toArray()));
        enabledCombo.setEnabled(!strings.isEmpty());
        selectedCombo.setEnabled(!strings.isEmpty());
    }
    
    
    void setMode(Mode mode) {
        this.mode = mode;
        if(mode == Mode.Form) {
            actionToEdit.setVisible(true);
            actionsCombo.setVisible(true);
            actionsLabel.setVisible(false);
            classLabel.setVisible(true);
            classField.setVisible(false);
            targetClassButton.setVisible(false);
            scopeCombo.setVisible(false);
            methodLabel.setVisible(true);
            methodField.setVisible(false);
            backgroundTaskLabel.setVisible(false);
            backgroundTaskCheckbox.setVisible(true);
            resetFields();
        }
        if(mode == Mode.NewActionForm) {
            newAction =  new ProxyAction();
            actionToEdit.setVisible(true);
            setNewActionCreated(true);
            classField.setText(scopeClasses.get(ProxyAction.Scope.Form));
            classField.setVisible(false);
            scopeCombo.setVisible(true);
        }
        if(mode == Mode.Global) {
            actionToEdit.setVisible(true);
            actionsCombo.setVisible(false);
            actionsLabel.setVisible(true);
            classLabel.setVisible(true);
            classField.setVisible(false);
            targetClassButton.setVisible(false);
            scopeCombo.setVisible(false);
            methodLabel.setVisible(true);
            methodField.setVisible(false);
            backgroundTaskLabel.setVisible(false);
            backgroundTaskCheckbox.setVisible(true);
        }
        if(mode == Mode.NewActionGlobal) {
            newAction = new ProxyAction();
            actionsLabel.setVisible(false);
            actionToEdit.setVisible(false);
            setNewActionCreated(true);
            classField.setText("");
            classField.setVisible(false);
            classLabel.setVisible(true);
            targetClassButton.setVisible(true);
            scopeCombo.setVisible(false);
        }
    }
    
    private void setAcceleratorPanelEnabled(boolean b) {
        acceleratorText.setEnabled(b);
        altCheckbox.setEnabled(b);
        controlCheckbox.setEnabled(b);
        shiftCheckbox.setEnabled(b);
        metaCheckbox.setEnabled(b);
    }
    
    private void clearAcceleratorPanel() {
        acceleratorText.setText("");
        altCheckbox.setSelected(false);
        controlCheckbox.setSelected(false);
        metaCheckbox.setSelected(false);
        shiftCheckbox.setSelected(false);
    }
    
    private void updateFieldsFromAction(final ProxyAction act) {
        if(act == null) {
            clearFieldsForNull();
        } else {
            textField.setEnabled(true);
            tooltipField.setEnabled(true);
            acceleratorListener.setEnabled(true);
            setAcceleratorPanelEnabled(true);
            iconButtonLarge.setEnabled(true);
            setIconButtonLarge.setEnabled(true);
            iconButtonSmall.setEnabled(true);
            setIconButtonSmall.setEnabled(true);
            selectedTextfield.setEnabled(true);
            enabledTextfield.setEnabled(true);
            backgroundTaskCheckbox.setEnabled(true);
            updatePropertyCombos(act);
        }
        
        setFromActionProperty(textField,act,Action.NAME);
        setFromActionProperty(tooltipField,act,Action.SHORT_DESCRIPTION);
        acceleratorListener.setCurrentKeyStroke((KeyStroke) act.getValue(javax.swing.Action.ACCELERATOR_KEY));
        
        
        StringBuffer sig = new StringBuffer();
        sig.append("@Action"); // NOI18N
        if(act.isTaskEnabled()) {
            sig.append(" Task"); // NOI18N
        } else {
            sig.append(" void"); // NOI18N
        }
        sig.append(" " + act.getId()); // NOI18N
        sig.append("()"); // NOI18N
        
        if(act == null) {
            methodLabel.setText(""); // NOI18N
            classLabel.setText(""); // NOI18N
            backgroundTaskLabel.setText(""); // NOI18N
        } else {
            actionsLabel.setText(act.getId());
            methodLabel.setText(sig.toString());
            classLabel.setText(act.getClassname());
            backgroundTaskLabel.setText(act.isTaskEnabled() ? getLocalizedString("yes") : getLocalizedString("no"));
            backgroundTaskCheckbox.setSelected(act.isTaskEnabled());
        }

        smallIconName = (String) act.getValue(Action.SMALL_ICON +".IconName"); // NOTI18N
        largeIconName = (String) act.getValue(LARGE_ICON_KEY +".IconName"); // NOTI18N
        if(act.getValue(Action.SMALL_ICON) != null) {
            iconButtonSmall.setIcon((Icon)act.getValue(Action.SMALL_ICON));
            iconButtonSmall.setText(null);
        } else {
            iconButtonSmall.setIcon(null);
            iconButtonSmall.setText("..."); // NOI18N
        }
        if(act.getValue(LARGE_ICON_KEY) != null) {
            iconButtonLarge.setIcon((Icon)act.getValue(LARGE_ICON_KEY));
            iconButtonLarge.setText(null);
        } else {
            iconButtonLarge.setIcon(null);
            iconButtonLarge.setText("..."); // NOI18N
        }
        
        blockingType.setEnabled(act.isTaskEnabled());
        blockingDialogText.setEnabled(act.isTaskEnabled());
        blockingDialogTitle.setEnabled(act.isTaskEnabled());
        if(act.isTaskEnabled()) {
            if(act.getBlockingType()!= null) {
                blockingType.setSelectedItem(act.getBlockingType());
            }
        } else {
            blockingType.setSelectedItem(NbBundle.getMessage(ActionPropertyEditorPanel.class,"BlockingTypeNone")); // NOI18N
        }
        
        setFromActionProperty(blockingDialogText,act,"BlockingDialog.message"); //NOI18N
        setFromActionProperty(blockingDialogTitle,act,"BlockingDialog.title"); //NOI18N
        if(act.getSelectedName()!=null) {
            selectedTextfield.setText(act.getSelectedName());
        } else {
            selectedTextfield.setText(null);
        }
        if(act.getEnabledName()!=null) {
            enabledTextfield.setText(act.getEnabledName());
        } else {
            enabledTextfield.setText(null);
        }
    }
    
    private void setFromActionProperty(JTextField textField, ProxyAction act, String key) {
        if(act.getValue(key)== null) {
            textField.setText(""); // NOI18N
        } else {
            textField.setText(""+act.getValue(key)); // NOI18N
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        textField = new javax.swing.JTextField();
        tooltipField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        iconButtonSmall = new IconButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        iconButtonLarge = new IconButton();
        jPanel7 = new javax.swing.JPanel();
        acceleratorText = new javax.swing.JTextField();
        clearAccelButton = new javax.swing.JButton();
        controlCheckbox = new javax.swing.JCheckBox();
        shiftCheckbox = new javax.swing.JCheckBox();
        altCheckbox = new javax.swing.JCheckBox();
        metaCheckbox = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        setIconButtonSmall = new javax.swing.JButton();
        setIconButtonLarge = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        enabledTextfield = new javax.swing.JTextField();
        selectedTextfield = new javax.swing.JTextField();
        blockingType = new javax.swing.JComboBox();
        blockingDialogTitle = new javax.swing.JTextField();
        blockingDialogText = new javax.swing.JTextField();
        enabledCombo = new javax.swing.JComboBox();
        selectedCombo = new javax.swing.JComboBox();
        actionToEdit = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        classLabel = new javax.swing.JLabel();
        classField = new javax.swing.JTextField();
        targetClassButton = new javax.swing.JButton();
        scopeCombo = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        methodLabel = new javax.swing.JLabel();
        methodField = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        backgroundTaskLabel = new javax.swing.JLabel();
        backgroundTaskCheckbox = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        actionsCombo = new javax.swing.JComboBox();
        actionsLabel = new javax.swing.JLabel();

        jPanel2.setOpaque(false);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel2.text_1")); // NOI18N

        textField.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "textField.text")); // NOI18N

        tooltipField.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "tooltipField.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel5.text")); // NOI18N

        iconButtonSmall.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonSmall.setBorder(null);
        iconButtonSmall.setContentAreaFilled(false);
        iconButtonSmall.setPreferredSize(new java.awt.Dimension(48, 48));

        jLabel7.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel7.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel4.text_1")); // NOI18N

        iconButtonLarge.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonLarge.setBorder(null);
        iconButtonLarge.setOpaque(false);
        iconButtonLarge.setPreferredSize(new java.awt.Dimension(64, 64));

        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        acceleratorText.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "acceleratorText.text")); // NOI18N

        clearAccelButton.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.clearAccelButton.text")); // NOI18N
        clearAccelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAccelButtonActionPerformed(evt);
            }
        });

        controlCheckbox.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox1.text")); // NOI18N
        controlCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        shiftCheckbox.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox2.text")); // NOI18N
        shiftCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        altCheckbox.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox3.text")); // NOI18N
        altCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        metaCheckbox.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox4.text")); // NOI18N
        metaCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel13.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jLabel13.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(controlCheckbox)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, altCheckbox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .add(jLabel13))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(metaCheckbox)
                    .add(shiftCheckbox)
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(acceleratorText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(clearAccelButton)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(controlCheckbox)
                    .add(shiftCheckbox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(altCheckbox)
                    .add(metaCheckbox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(acceleratorText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(clearAccelButton))
                .add(12, 12, 12))
        );

        jLabel10.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jLabel10.text")); // NOI18N

        setIconButtonSmall.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jButton1.text")); // NOI18N

        setIconButtonLarge.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jButton2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel4)
                            .add(jLabel7))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(tooltipField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                            .add(textField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel5)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jLabel10)
                                .add(16, 16, 16)
                                .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(85, 85, 85)
                                .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 67, Short.MAX_VALUE)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(setIconButtonSmall)
                            .add(setIconButtonLarge))
                        .add(20, 20, 20))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(textField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(tooltipField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5)
                    .add(setIconButtonSmall))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(setIconButtonLarge)
                    .add(jLabel10)
                    .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(34, 34, 34))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setOpaque(false);

        jLabel6.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel6.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel3.text_1")); // NOI18N

        jLabel1.setLabelFor(blockingType);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel1.text_1")); // NOI18N

        jLabel11.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel11.text")); // NOI18N

        jLabel8.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel8.text")); // NOI18N

        blockingType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Action", "Component", "Window", "Application" }));
        blockingType.setEnabled(false);
        blockingType.setOpaque(false);

        blockingDialogTitle.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "blockingDialogTitle.text")); // NOI18N
        blockingDialogTitle.setEnabled(false);

        blockingDialogText.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "blockingDialogText.text")); // NOI18N
        blockingDialogText.setEnabled(false);

        enabledCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        selectedCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel8)
                        .add(36, 36, 36)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(selectedCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(enabledTextfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                            .add(enabledCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(selectedTextfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(jLabel11))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel6)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(blockingType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(blockingDialogTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                            .add(blockingDialogText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(enabledCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(enabledTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(selectedCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectedTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(blockingType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(blockingDialogTitle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(blockingDialogText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(118, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        actionToEdit.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.actionToEdit.text")); // NOI18N

        jLabel9.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel9.text")); // NOI18N

        jLabel12.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel12.text")); // NOI18N

        jLabel14.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel14.text")); // NOI18N

        jLabel17.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel17.text")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        classLabel.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.classLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(classLabel, gridBagConstraints);

        classField.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "classField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(classField, gridBagConstraints);

        targetClassButton.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.targetClassButton.text")); // NOI18N
        targetClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetClassButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(targetClassButton, gridBagConstraints);

        scopeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(scopeCombo, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        methodLabel.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "methodLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(methodLabel, gridBagConstraints);

        methodField.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "methodField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(methodField, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        backgroundTaskLabel.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.backgroundTaskLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel5.add(backgroundTaskLabel, gridBagConstraints);

        backgroundTaskCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        backgroundTaskCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundTaskCheckboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel5.add(backgroundTaskCheckbox, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        actionsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "save", "open", "new", "exit", "cut" }));
        actionsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionsComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel6.add(actionsCombo, gridBagConstraints);

        actionsLabel.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "actionsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel6.add(actionsLabel, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel14)
                    .add(jLabel17)
                    .add(jLabel12)
                    .add(actionToEdit)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(actionToEdit))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel12)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel14)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jLabel17))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void targetClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetClassButtonActionPerformed

    ClassPathFileChooser cp = new ClassPathFileChooser(sourceFile, new ClassPathFileChooser.Filter() {
        public boolean accept(FileObject file) {
            return true;
        }
    },true,true);

    cp.getDialog(getLocalizedString("classChooserDialogTitle"), null).setVisible(true);
    if(cp.getSelectedFile() != null) {
        selectedSourceFile = cp.getSelectedFile();
        String selectedClass = AppFrameworkSupport.getClassNameForFile(cp.getSelectedFile());
        classField.setText(selectedClass);
        classLabel.setText(selectedClass);
    }
}//GEN-LAST:event_targetClassButtonActionPerformed

private void backgroundTaskCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundTaskCheckboxActionPerformed
    if(backgroundTaskCheckbox.isVisible()) {
        blockingType.setEnabled(backgroundTaskCheckbox.isSelected());
        blockingDialogText.setEnabled(backgroundTaskCheckbox.isSelected());
        blockingDialogTitle.setEnabled(backgroundTaskCheckbox.isSelected());
    }
}//GEN-LAST:event_backgroundTaskCheckboxActionPerformed

    private void clearAccelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAccelButtonActionPerformed
        // clear the accelerator because you can't actually use backspace to clear it
        acceleratorListener.clearFields();
        clearAcceleratorPanel();
    }//GEN-LAST:event_clearAccelButtonActionPerformed
    
    
        
    private void actionsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionsComboActionPerformed
        if(actionsCombo.getSelectedItem() == NEW_ACTION) {
            setMode(Mode.NewActionForm);
        } else {
            if(!isChanging) {
                firePropertyChange("action",null,getSelectedAction()); // NOI18N
            }
        }
    }//GEN-LAST:event_actionsComboActionPerformed

    
    void setParsedActions(Map<ProxyAction.Scope, List<ProxyAction>> actionMap) {
        this.parsedActions = actionMap;
        List<ProxyAction> actions = new ArrayList<ProxyAction>();
        actions.addAll(parsedActions.get(ProxyAction.Scope.Application));
        actions.addAll(parsedActions.get(ProxyAction.Scope.Form));
        actions.add(NEW_ACTION);
        actionsCombo.setModel(new DefaultComboBoxModel(actions.toArray()));
        if(!isChanging) {
            firePropertyChange("action",null,getSelectedAction()); // NOI18N
        }
    }
    
    // returns the selected action with the properties filled in from the
    // text fields on the form
    ProxyAction getUpdatedAction() {
        ProxyAction act = getSelectedAction();
        act.putValue(Action.NAME, textField.getText());
        act.putValue(Action.SHORT_DESCRIPTION, tooltipField.getText());
        act.putValue(Action.ACCELERATOR_KEY, acceleratorListener.getCurrentKeyStroke());
        if (backgroundTaskCheckbox.isVisible()) {
            act.setTaskEnabled(backgroundTaskCheckbox.isSelected());
        }
        if(act.isTaskEnabled()) {
            act.setBlockingType(getSelectedBlockingType());
            setFromTextField(blockingDialogText,act,"BlockingDialog.message"); // NOI18N
            setFromTextField(blockingDialogTitle,act,"BlockingDialog.title"); // NOI18N
        }
        if(selectedTextfield.getText() != null) {
            act.setSelectedName(selectedTextfield.getText());
        }
        if(enabledTextfield.getText() != null) {
            act.setEnabledName(enabledTextfield.getText());
        }
        act.putValue(Action.SMALL_ICON+".IconName",  smallIconName); // NOTI18N
        act.putValue(LARGE_ICON_KEY+".IconName",  largeIconName); // NOTI18N
        return act;
    }
    
    private void setFromTextField(JTextField textField, ProxyAction act, String key) {
        if(textField.getText() != null && !textField.getText().trim().equals("")) {
            act.putValue(key,textField.getText());
        }
    }
    
    private String getSelectedClassname() {
        if(mode == Mode.NewActionForm) {
            ProxyAction.Scope scope = (Scope) scopeCombo.getSelectedItem();
            return scopeClasses.get(scope);
        }
        if(mode == Mode.NewActionGlobal) {
            return classLabel.getText();
        }
        return null;
    }
    
    ProxyAction getNewAction() {
        //ProxyAction act = new ProxyAction(classField.getText(), methodField.getText());
        ProxyAction act = newAction;
        //act.setClassname(classField.getText());
        act.setClassname(getSelectedClassname());
        //act.setMethodname(methodField.getText());
        act.setId(methodField.getText());
        act.putValue(Action.NAME,textField.getText());
        act.putValue(Action.SHORT_DESCRIPTION,tooltipField.getText());
        //act.setTaskEnabled(returnsTask);
        act.setTaskEnabled(backgroundTaskCheckbox.isSelected());
        
        act.putValue(Action.SMALL_ICON+".IconName",this.smallIconName); // NOI18N
        act.putValue(LARGE_ICON_KEY+".IconName",this.largeIconName); // NOI18N
        act.putValue(Action.SMALL_ICON,iconButtonSmall.getIcon());
        act.putValue(LARGE_ICON_KEY,iconButtonLarge.getIcon());
        
        if(act.isTaskEnabled()) {
            act.setBlockingType(getSelectedBlockingType());
            setFromTextField(blockingDialogText,   act, "BlockingDialog.message"); // NOI18N
            setFromTextField(blockingDialogTitle,  act, "BlockingDialog.title"); // NOI18N
        }
        
        act.putValue(Action.ACCELERATOR_KEY, acceleratorListener.getCurrentKeyStroke());
        if(selectedTextfield.getText() != null) {
            act.setSelectedName(selectedTextfield.getText());
        }
        if(enabledTextfield.getText() != null) {
            act.setEnabledName(enabledTextfield.getText());
        }
        return act;
    }
    
    private ProxyAction.BlockingType getSelectedBlockingType() {
        return (ProxyAction.BlockingType)blockingType.getSelectedItem();
    }
    
    /*
    ProxyAction.Scope getSelectedScope() {
        return newActionScope;
    }*/
    
    ProxyAction getSelectedAction() {
        if(this.mode == Mode.NewActionGlobal) {
            return newAction;
        }
        
        
        // this stuff needs to be revisited
        if(globalMode) {
            return globalAction;
        }
        if(isNewActionCreated()) {
            return newAction;
        }
        Object action = actionsCombo.getSelectedItem();
        if(action instanceof ProxyAction) {
            return (ProxyAction)action;
        }
        return null;
    }
    
    public void updatePanel(Map<ProxyAction.Scope, List<ProxyAction>> actionMap, ProxyAction selectedAction,
            Map<ProxyAction.Scope, String> scopeClasses, String componentName, FileObject sourceFile) {
        isChanging = true;
        setParsedActions(actionMap);
        setSelectedAction(selectedAction);
        this.scopeClasses = scopeClasses;
        this.sourceFile = sourceFile;
        isChanging = false;
    }
    
    private void setSelectedAction(ProxyAction act) {
        if(act != null) {
            if(globalMode) {
                globalAction = act;
            }
            setNewActionCreated(false);
            actionsCombo.setEnabled(true);
            //scopeCombo.setEnabled(true);
            //scopeCombo.setSelectedItem(act.getScope());
            //set the selection action by finding the right match
            for(int i=0; i<actionsCombo.getModel().getSize(); i++) {
                Object o = actionsCombo.getModel().getElementAt(i);
                if (o != null) {
                    ProxyAction act2 = (ProxyAction)o;
                    if(act2.getId().equals(act.getId())) {
                        actionsCombo.setSelectedItem(act2);
                        actionsLabel.setText(act2.getId());
                        break;
                    }
                }
            }
            updateFieldsFromAction(act);
        } else {
            clearFields();
            clearFieldsForNull();
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTextField acceleratorText;
    private javax.swing.JLabel actionToEdit;
    private javax.swing.JComboBox actionsCombo;
    private javax.swing.JLabel actionsLabel;
    public javax.swing.JCheckBox altCheckbox;
    private javax.swing.JCheckBox backgroundTaskCheckbox;
    private javax.swing.JLabel backgroundTaskLabel;
    private javax.swing.JTextField blockingDialogText;
    private javax.swing.JTextField blockingDialogTitle;
    private javax.swing.JComboBox blockingType;
    private javax.swing.JTextField classField;
    private javax.swing.JLabel classLabel;
    private javax.swing.JButton clearAccelButton;
    public javax.swing.JCheckBox controlCheckbox;
    private javax.swing.JComboBox enabledCombo;
    private javax.swing.JTextField enabledTextfield;
    private javax.swing.JButton iconButtonLarge;
    private javax.swing.JButton iconButtonSmall;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JCheckBox metaCheckbox;
    private javax.swing.JTextField methodField;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JComboBox selectedCombo;
    private javax.swing.JTextField selectedTextfield;
    private javax.swing.JButton setIconButtonLarge;
    private javax.swing.JButton setIconButtonSmall;
    public javax.swing.JCheckBox shiftCheckbox;
    private javax.swing.JButton targetClassButton;
    private javax.swing.JTextField textField;
    private javax.swing.JTextField tooltipField;
    // End of variables declaration//GEN-END:variables
    
    
    private void clearFields() {
        textField.setText(""); // NOI18N
        acceleratorListener.clearFields();
        tooltipField.setText(""); // NOI18N
        newMethodName = ""; // NOI18N
        iconButtonSmall.setIcon(null);
        iconButtonLarge.setIcon(null);
    }
    
    private void clearFieldsForNull() {
        textField.setEnabled(false);
        textField.setText("");
        tooltipField.setEnabled(false);
        tooltipField.setText("");
        acceleratorListener.setEnabled(false);
        setAcceleratorPanelEnabled(false);
        clearAcceleratorPanel();
        iconButtonLarge.setEnabled(false);
        iconButtonLarge.setIcon(null);
        setIconButtonLarge.setEnabled(false);
        iconButtonSmall.setEnabled(false);
        iconButtonSmall.setIcon(null);
        setIconButtonSmall.setEnabled(false);
        selectedTextfield.setEnabled(false);
        selectedTextfield.setText("");
        enabledTextfield.setText("");
        enabledTextfield.setEnabled(false);
        
        blockingDialogText.setEnabled(false);
        blockingDialogText.setText("");
        blockingDialogTitle.setEnabled(false);
        blockingDialogTitle.setText("");
        blockingType.setEnabled(false);
        
        backgroundTaskCheckbox.setEnabled(false);
        backgroundTaskCheckbox.setSelected(false);
    }
    
    private void clearFieldsForNewAction() {
        textField.setEnabled(true);
        tooltipField.setEnabled(true);
        acceleratorListener.setEnabled(true);
        setAcceleratorPanelEnabled(true);
        iconButtonLarge.setEnabled(true);
        setIconButtonLarge.setEnabled(true);
        iconButtonSmall.setEnabled(true);
        setIconButtonSmall.setEnabled(true);
        selectedTextfield.setEnabled(true);
        selectedTextfield.setText("");
        enabledTextfield.setEnabled(true);
        enabledTextfield.setText("");
        actionsCombo.setEnabled(false);
        textField.setText(""); // NOI18N
        acceleratorListener.clearFields();
        tooltipField.setText(""); // NOI18N
        newMethodName = ""; // NOI18N
        iconButtonSmall.setIcon(null);
        iconButtonLarge.setIcon(null);
        // josh: is this next line correct?
        blockingType.setEnabled(false);
        
        classLabel.setText("");
        classLabel.setVisible(false);
        classField.setText("");
        classField.setVisible(true);
        
        methodLabel.setText("");
        methodLabel.setVisible(false);
        methodField.setText("");
        methodField.setVisible(true);
        
        backgroundTaskLabel.setText("");
        backgroundTaskLabel.setVisible(false);
        backgroundTaskCheckbox.setSelected(false);
        backgroundTaskCheckbox.setEnabled(true);
        backgroundTaskCheckbox.setVisible(true);
    }
    
    public void resetFields() {
        this.setNewActionCreated(false);
        this.viewSource = false;
        this.actionPropertiesUpdated = false;
    }
    
    public void setNewActionCreated(boolean newActionCreated) {
        this.newActionCreated = newActionCreated;
        if(newActionCreated) {
            clearFieldsForNewAction();
        } else {
            //scopeCombo.setEnabled(true);
            blockingType.setEnabled(true);
            actionsCombo.setEnabled(true);
        }
    }
     
    boolean isMethodNonEmpty() {
        newMethodName = methodField.getText();
        if(newMethodName == null) {
            return false;
        }
        if(newMethodName.trim().equals("")) { //NOI18N
            return false;
        }
        return true;
    }
    
    boolean doesMethodContainBadChars() {
        newMethodName = methodField.getText();        
        if(newMethodName.contains(" ")) { //NOI18N
            return true;
        }
        if(newMethodName.matches("^\\d.*")) { //NOI18N
            return true;
        }
        if(!Utilities.isJavaIdentifier(newMethodName)) {
            return true;
        }
        return false;
    }
    
    boolean isValidClassname() {
        String classname = getSelectedClassname();
        if(classname == null) { return false; }
        if(classname.trim().equals("")) { return false; }
        
        ActionManager am = ActionManager.getActionManager(sourceFile);
        if(am.getFileForClass(classname) == null) { return false; }
        return true;
    }
    
    boolean isDuplicateMethod() {
        newMethodName = methodField.getText();
        String classname = getSelectedClassname();
        if(classname == null) { return true; }
        if(classname.trim().equals("")) { return true; }
        
        
        ActionManager am = ActionManager.getActionManager(sourceFile);
        System.out.println("checking for dupe: " + newMethodName);
        if(am.isExistingMethod(classname, newMethodName)) {
            System.out.println("is dup");
            return true;
        }
        return false;
    }
    
    
    
    boolean canCreateNewAction() {
        newMethodName = methodField.getText();
        
        /*
        if(newMethodName == null) {
            return false;
        }
        if(newMethodName.trim().equals("")) { //NOI18N
            return false;
        }*/
        if(!isMethodNonEmpty()) { 
            return false;
        }
        
        /*
        if(newMethodName.contains(" ")) { //NOI18N
            return false;
        }
        if(newMethodName.matches("^\\d.*")) { //NOI18N
            return false;
        }*/
        if(doesMethodContainBadChars()) {
            return false;
        }
        
        /*
        String classname = getSelectedClassname();
        if(classname == null) { return false; }
        if(classname.trim().equals("")) { return false; }
        
        
        ActionManager am = ActionManager.getActionManager(sourceFile);
        if(am.isExistingMethod(classname, newMethodName)) {
            return false;
        }*/
        if(!isValidClassname()) {
            return false;
        }
        
        if(isDuplicateMethod()) {
            return false;
        }
        
        return true;
    }
        
    String getNewMethodName() {
        return methodField.getText();
    }
        
    private class DirtyDocumentListener implements DocumentListener {
        
        public void changedUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
        
        public void insertUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
        
        public void removeUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
    }
    
    private class IconButtonListener implements ActionListener {
        
        private FormProperty property;
        private JButton iconButton;
        private String iconKey;
        
        public IconButtonListener(FormProperty property, JButton iconButton, String iconKey) {
            super();
            this.property = property;
            this.iconButton = iconButton;
            this.iconKey = iconKey;
        }
        
        public void actionPerformed(ActionEvent e) {
            ProxyAction action = getSelectedAction();
            IconEditor iconEditor = new IconEditor();
            // if this is a dir, then use the application class instead
            if(sourceFile.isFolder()) {
                sourceFile = ActionManager.getActionManager(sourceFile).getApplicationClassFile();
            }
            iconEditor.setSourceFile(sourceFile);
            
            if(Action.SMALL_ICON.equals(iconKey)){
                if (smallIconName != null) {
                    iconEditor.setAsText(smallIconName);
                }
            } else {
                if (largeIconName != null) {
                    iconEditor.setAsText(largeIconName);
                }
            }
            DialogDescriptor dd = new DialogDescriptor(iconEditor.getCustomEditor(), NbBundle.getMessage(ActionPropertyEditorPanel.class, "CTL_SelectIcon_Title"));
            if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                IconEditor.NbImageIcon nbIcon = (IconEditor.NbImageIcon) iconEditor.getValue();
                Icon icon = nbIcon != null ? nbIcon.getIcon() : null;
                iconButton.setIcon(icon);
                iconButton.setText(icon == null ? "..." : null);
                action.putValue(iconKey, icon);
                String iconName = nbIcon != null ? nbIcon.getName() : null;
                if(Action.SMALL_ICON.equals(iconKey)) {
                    smallIconName = iconName;
                } else {
                    largeIconName = iconName;
                }
                action.putValue(iconKey+".IconName", iconName); // NOI18N
            }
        }
    }
    
    private void setupAccelField() {
        acceleratorListener = new AcceleratorKeyListener(this);
            // add the special listener
        acceleratorText.addKeyListener(acceleratorListener);
        // turn off focus keys
        acceleratorText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        acceleratorText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    }
    
    
    public boolean isNewActionCreated() {
        return newActionCreated;
    }
    
    boolean isViewSource() {
        return this.viewSource;
    }
    
    boolean isActionPropertiesUpdated() {
        boolean task_changed = false;

        boolean accel_changed = false;
        
        if(getSelectedAction() != null) {
            if(getSelectedAction().isTaskEnabled() != backgroundTaskCheckbox.isSelected()) {
                task_changed = true;
            }
            if(acceleratorListener.getCurrentKeyStroke() == null && getSelectedAction().getValue() != null) {
                accel_changed = true;
            } else if(!acceleratorListener.getCurrentKeyStroke().equals(getSelectedAction().getValue(Action.ACCELERATOR_KEY))) {
                accel_changed = true;
            }
        }
        return this.actionPropertiesUpdated || task_changed || accel_changed;
    }
    
    
    public FileObject getSelectedSourceFile() {
        return selectedSourceFile;
    }
    
    
    public static void p(String s) {
        System.out.println(s);
    }

    private String getLocalizedString(String key) {
        return NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel."+key);
    }
}

