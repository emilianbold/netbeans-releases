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
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
    
    private void updateFieldsFromAction(final ProxyAction act) {
        if(act == null) {
            clearFieldsForNull();
        } else {
            textField.setEnabled(true);
            tooltipField.setEnabled(true);
            acceleratorText.setEnabled(true);
            iconButtonLarge.setEnabled(true);
            iconButtonSmall.setEnabled(true);
            selectedTextfield.setEnabled(true);
            enabledTextfield.setEnabled(true);
            backgroundTaskCheckbox.setEnabled(true);
        }
        
        setFromActionProperty(textField,act,Action.NAME);
        setFromActionProperty(tooltipField,act,Action.SHORT_DESCRIPTION);
        setFromActionProperty(acceleratorText,act,Action.ACCELERATOR_KEY);
        
        
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
        acceleratorText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        iconButtonSmall = new IconButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        iconButtonLarge = new IconButton();
        clearAccelButton = new javax.swing.JButton();
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

        acceleratorText.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "acceleratorText.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel5.text")); // NOI18N

        iconButtonSmall.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonSmall.setBorder(null);
        iconButtonSmall.setContentAreaFilled(false);

        jLabel7.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel7.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel4.text_1")); // NOI18N

        iconButtonLarge.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonLarge.setBorder(null);
        iconButtonLarge.setOpaque(false);

        clearAccelButton.setText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.clearAccelButton.text")); // NOI18N
        clearAccelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAccelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel4)
                    .add(jLabel7)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, tooltipField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, textField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(acceleratorText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 37, Short.MAX_VALUE)
                                .add(clearAccelButton)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
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
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(clearAccelButton)
                    .add(acceleratorText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addContainerGap(37, Short.MAX_VALUE))
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

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel8)
                        .add(36, 36, 36)
                        .add(enabledTextfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel11)
                        .add(33, 33, 33)
                        .add(selectedTextfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel6)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(blockingType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(blockingDialogText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                            .add(blockingDialogTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(enabledTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectedTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11))
                .add(26, 26, 26)
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
                .addContainerGap(45, Short.MAX_VALUE))
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
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
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
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel17)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
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
        //validate();
        //SwingUtilities.getWindowAncestor(this).pack();
    }
}//GEN-LAST:event_targetClassButtonActionPerformed

private void backgroundTaskCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundTaskCheckboxActionPerformed
    if(backgroundTaskCheckbox.isVisible()) {
        blockingType.setEnabled(backgroundTaskCheckbox.isSelected());
        blockingDialogText.setEnabled(backgroundTaskCheckbox.isSelected());
        blockingDialogTitle.setEnabled(backgroundTaskCheckbox.isSelected());
    }
    // TODO add your handling code here:
}//GEN-LAST:event_backgroundTaskCheckboxActionPerformed

    private void clearAccelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAccelButtonActionPerformed
        // clear the accelerator because you can't actually use backspace to clear it
        acceleratorText.setText(""); // NOI18N
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
        act.putValue(Action.NAME,textField.getText());
        act.putValue(Action.SHORT_DESCRIPTION,tooltipField.getText());
        if(acceleratorText.getText() != null && !acceleratorText.getText().equals("")) {
            KeyStroke key = KeyStroke.getKeyStroke(acceleratorText.getText());
            act.putValue(Action.ACCELERATOR_KEY,key);
        } else {
            act.putValue(Action.ACCELERATOR_KEY,null);
        }
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
        
        if(acceleratorText.getText() != null && !acceleratorText.getText().equals("")) { // NOI18N
            act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(acceleratorText.getText()));
        }
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
    private javax.swing.JTextField acceleratorText;
    private javax.swing.JLabel actionToEdit;
    private javax.swing.JComboBox actionsCombo;
    private javax.swing.JLabel actionsLabel;
    private javax.swing.JCheckBox backgroundTaskCheckbox;
    private javax.swing.JLabel backgroundTaskLabel;
    private javax.swing.JTextField blockingDialogText;
    private javax.swing.JTextField blockingDialogTitle;
    private javax.swing.JComboBox blockingType;
    private javax.swing.JTextField classField;
    private javax.swing.JLabel classLabel;
    private javax.swing.JButton clearAccelButton;
    private javax.swing.JTextField enabledTextfield;
    private javax.swing.JButton iconButtonLarge;
    private javax.swing.JButton iconButtonSmall;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField methodField;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JTextField selectedTextfield;
    private javax.swing.JButton targetClassButton;
    private javax.swing.JTextField textField;
    private javax.swing.JTextField tooltipField;
    // End of variables declaration//GEN-END:variables
    
    
    private void clearFields() {
        textField.setText(""); // NOI18N
        acceleratorText.setText(""); // NOI18N
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
        acceleratorText.setEnabled(false);
        acceleratorText.setText("");
        iconButtonLarge.setEnabled(false);
        iconButtonLarge.setIcon(null);
        iconButtonSmall.setEnabled(false);
        iconButtonSmall.setIcon(null);
        selectedTextfield.setEnabled(false);
        selectedTextfield.setText("");
        enabledTextfield.setEnabled(false);
        enabledTextfield.setText("");
        
        blockingDialogText.setEnabled(false);
        blockingDialogText.setText("");
        blockingDialogTitle.setEnabled(false);
        blockingDialogTitle.setText("");
        blockingType.setEnabled(false);
        
        backgroundTaskCheckbox.setEnabled(false);
    }
    
    private void clearFieldsForNewAction() {
        textField.setEnabled(true);
        tooltipField.setEnabled(true);
        acceleratorText.setEnabled(true);
        iconButtonLarge.setEnabled(true);
        iconButtonSmall.setEnabled(true);
        selectedTextfield.setEnabled(true);
        enabledTextfield.setEnabled(true);
        actionsCombo.setEnabled(false);
        //scopeCombo.setEnabled(false);
        textField.setText(""); // NOI18N
        acceleratorText.setText(""); // NOI18N
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
        // turn off foucs keys
        acceleratorText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        acceleratorText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        acceleratorText.addKeyListener(new AcceleratorKeyListener());
    }
    
    
    public boolean isNewActionCreated() {
        return newActionCreated;
    }
    
    boolean isViewSource() {
        return this.viewSource;
    }
    
    boolean isActionPropertiesUpdated() {
        return this.actionPropertiesUpdated;
    }
    
    
    public FileObject getSelectedSourceFile() {
        return selectedSourceFile;
    }
    
/*    
    public static void p(String s) {
        System.out.println(s);
    }
 */
    private String getLocalizedString(String key) {
        return NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel."+key);
    }
}

