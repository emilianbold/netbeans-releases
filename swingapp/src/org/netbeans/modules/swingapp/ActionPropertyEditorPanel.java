/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * This is the actual dialog used for editing actions. It lets the user set
 * almost any attribute on an action. The action will not be saved from this
 * dialog, however. When the user presses the Okay button (defined in the NB supplied
 * property editor dialog) the ActionEditor class will do the actual saving.
 * @author  joshua.marinacci@sun.com
 */
public class ActionPropertyEditorPanel extends javax.swing.JPanel implements HelpCtx.Provider {
    
    public static final String LARGE_ICON_KEY = "SwingLargeIconKey"; // NOI18N
    
    private Map<ProxyAction.Scope, List<ProxyAction>> parsedActions;
    private boolean newActionCreated = false;
    private boolean actionPropertiesUpdated = false;
    private boolean viewSource = false;
    private String newMethodName = ""; // NOI18N
    private boolean isChanging = false;
    private Map<ProxyAction.Scope, String> scopeClasses = new HashMap<ProxyAction.Scope, String>();
    private FileObject sourceFile;
    private String smallIconName = null;
    private String largeIconName = null;
    private ProxyAction newAction = null;
    private ProxyAction globalAction = null;
    private boolean globalMode = false;
    private ProxyAction NEW_ACTION = new ProxyAction("-newaction-","-id-","-methodname-"); // NOI18N
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
            @Override
            public void actionPerformed(ActionEvent e) {
                actionPropertiesUpdated = true;
            }
        });
        
        DocumentListener dirtyListener = new DirtyDocumentListener();
        textField.getDocument().addDocumentListener(dirtyListener);
        tooltipField.getDocument().addDocumentListener(dirtyListener);
        acceleratorText.getDocument().addDocumentListener(dirtyListener);
        
        this.addPropertyChangeListener("action", new PropertyChangeListener() { // NOI18N
            @Override
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
        
        ((IconButton)iconButtonLarge).setIconText(NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.iconButtonLarge.text")); // NOI18N
        ((IconButton)iconButtonSmall).setIconText(NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.iconButtonSmall.text")); // NOI18N
        iconButtonSmall.addActionListener(new IconButtonListener(property,iconButtonSmall, Action.SMALL_ICON));
        iconButtonLarge.addActionListener(new IconButtonListener(property,iconButtonLarge, LARGE_ICON_KEY));
        setIconButtonSmall.addActionListener(new IconButtonListener(property,iconButtonSmall, Action.SMALL_ICON));
        setIconButtonLarge.addActionListener(new IconButtonListener(property,iconButtonLarge, LARGE_ICON_KEY));
        
        scopeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if (value!=null) {
                    ProxyAction.Scope scope = (ProxyAction.Scope)value;
                    String className = scopeClasses.get(scope);
                    if(className != null) {
                        ((JLabel)comp).setText(scope.toString() + ": " + className);
                    } else {
                        ((JLabel)comp).setText(scope.toString());
                    }
                }
                return comp;
            }
        });
        
        actionsCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                String text = getLocalizedString("noneAction"); // NOI18N
                if(value instanceof ProxyAction) {
                    ProxyAction act = (ProxyAction)value;
                    if(value == NEW_ACTION) {
                        text = getLocalizedString("createNewAction"); // NOI18N
                    } else {
                        text = act != null ? act.getId() : getLocalizedString("noneAction"); // NOI18N
                        if(act != null && act.isAppWide()) {
                            text += getLocalizedString("globalActionAppend"); // NOI18N
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
        updatePropertyCombos(null);
        
        ActionListener modifierListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                acceleratorListener.updateFromModifiers();
                updateState();
            }
        };
        altCheckbox.addActionListener(modifierListener);
        controlCheckbox.addActionListener(modifierListener);
        metaCheckbox.addActionListener(modifierListener);
        shiftCheckbox.addActionListener(modifierListener);
        
        acceleratorText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateState();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateState();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateState();
            }
        });
    }

    void updateState() {
        if (env != null) {
            if ("".equals(acceleratorText.getText()) // NOI18N
                    && (altCheckbox.isSelected() || controlCheckbox.isSelected() || metaCheckbox.isSelected() || shiftCheckbox.isSelected())) {
                env.setState(PropertyEnv.STATE_INVALID);
            } else {
                env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            }
        }
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
        
        // first none action combobox item
        strings.add(0, ""); // NOI18N
        
        enabledCombo.setModel(new DefaultComboBoxModel(strings.toArray()));
        selectedCombo.setModel(new DefaultComboBoxModel(strings.toArray()));
        if (act != null) {
            if (strings.contains(act.getEnabledName())) {
                enabledCombo.setSelectedItem(act.getEnabledName());
            }
            if (strings.contains(act.getSelectedName())) {
                selectedCombo.setSelectedItem(act.getSelectedName());
            }
        }
    }
    
    
    void setMode(Mode mode) {
        this.mode = mode;
        if(mode == Mode.Form) {
            actionToEdit.setVisible(true);
            actionsCombo.setVisible(true);
            actionsField.setVisible(false);
            classField.setVisible(true);
            classField.setEditable(false);
            scopeCombo.setVisible(false);
            targetClassButton.setVisible(false);
            methodField.setEditable(false);
            backgroundTaskCheckbox.setVisible(true);
            resetFields();
        }
        if(mode == Mode.NewActionForm) {
            newAction =  new ProxyAction();
            actionToEdit.setVisible(true);
            setNewActionCreated(true);
            classField.setVisible(false);
            scopeCombo.setVisible(true);
            targetClassButton.setVisible(false);
        }
        if(mode == Mode.Global) {
            actionToEdit.setVisible(true);
            actionsCombo.setVisible(false);
            actionsField.setVisible(true);
            classField.setVisible(true);
            classField.setEditable(false);
            scopeCombo.setVisible(false);
            targetClassButton.setVisible(false);
            methodField.setEditable(false);
            backgroundTaskCheckbox.setVisible(true);
        }
        if(mode == Mode.NewActionGlobal) {
            newAction = new ProxyAction();
            actionsCombo.setVisible(false);
            actionsField.setVisible(false);
            actionToEdit.setVisible(false);
            setNewActionCreated(true);
            classField.setVisible(true);
            classField.setEditable(true);
            scopeCombo.setVisible(false);
            targetClassButton.setVisible(true);
            methodField.setEditable(true);
        }
        actionToEdit.setLabelFor(actionsField.isVisible() ? actionsField : actionsCombo);
        jLabel9.setLabelFor(classField.isVisible() ? classField : scopeCombo);
    }
    
    private void setAcceleratorPanelEnabled(boolean b) {
        acceleratorText.setEnabled(b);
        altCheckbox.setEnabled(b);
        controlCheckbox.setEnabled(b);
        shiftCheckbox.setEnabled(b);
        metaCheckbox.setEnabled(b);
    }
    
    private void clearAcceleratorPanel() {
        acceleratorText.setText(""); // NOI18N
        altCheckbox.setSelected(false);
        controlCheckbox.setSelected(false);
        metaCheckbox.setSelected(false);
        shiftCheckbox.setSelected(false);
        updateState();
    }
    
    private void updateFieldsFromAction(final ProxyAction act) {
        textField.setEnabled(true);
        tooltipField.setEnabled(true);
        acceleratorListener.setEnabled(true);
        setAcceleratorPanelEnabled(true);
        iconButtonLarge.setEnabled(true);
        setIconButtonLarge.setEnabled(true);
        iconButtonSmall.setEnabled(true);
        setIconButtonSmall.setEnabled(true);
        selectedCombo.setEnabled(true);
        enabledCombo.setEnabled(true);
        backgroundTaskCheckbox.setEnabled(true);
        updatePropertyCombos(act);
        
        setFromActionProperty(textField,act,Action.NAME);
        setFromActionProperty(tooltipField,act,Action.SHORT_DESCRIPTION);
        acceleratorListener.setCurrentKeyStroke((KeyStroke) act.getValue(javax.swing.Action.ACCELERATOR_KEY));
        
        
        StringBuilder sig = new StringBuilder();
        sig.append("@Action"); // NOI18N
        if(act.isTaskEnabled()) {
            sig.append(" Task"); // NOI18N
        } else {
            sig.append(" void"); // NOI18N
        }
        sig.append(" ").append(act.getMethodName()); // NOI18N
        sig.append("()"); // NOI18N
        
        actionsField.setText(act.getId());
        classField.setText(act.getClassname());
        methodField.setText(sig.toString());
        backgroundTaskCheckbox.setSelected(act.isTaskEnabled());

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
        selectedCombo.setSelectedItem(act.getSelectedName());
        enabledCombo.setSelectedItem(act.getEnabledName());
        
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        setIconButtonSmall = new javax.swing.JButton();
        setIconButtonLarge = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        blockingType = new javax.swing.JComboBox();
        blockingDialogTitle = new javax.swing.JTextField();
        blockingDialogText = new javax.swing.JTextField();
        enabledCombo = new javax.swing.JComboBox();
        selectedCombo = new javax.swing.JComboBox();
        actionToEdit = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        backgroundTaskCheckbox = new javax.swing.JCheckBox();
        actionsCombo = new javax.swing.JComboBox();
        methodField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        targetClassButton = new javax.swing.JButton();
        scopeCombo = new javax.swing.JComboBox();
        classField = new javax.swing.JTextField();
        actionsField = new javax.swing.JTextField();

        jLabel2.setLabelFor(textField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel2.text_1")); // NOI18N

        textField.setColumns(15);

        tooltipField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel5.text")); // NOI18N

        iconButtonSmall.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonSmall.setBorder(null);
        iconButtonSmall.setContentAreaFilled(false);
        iconButtonSmall.setPreferredSize(new java.awt.Dimension(48, 48));

        jLabel7.setLabelFor(jPanel7);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel7.text")); // NOI18N

        jLabel4.setLabelFor(tooltipField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel4.text_1")); // NOI18N

        iconButtonLarge.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonLarge.setBorder(null);
        iconButtonLarge.setContentAreaFilled(false);
        iconButtonLarge.setPreferredSize(new java.awt.Dimension(64, 64));

        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        acceleratorText.setColumns(5);

        org.openide.awt.Mnemonics.setLocalizedText(clearAccelButton, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.clearAccelButton.text")); // NOI18N
        clearAccelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAccelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(controlCheckbox, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox1.text")); // NOI18N
        controlCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(shiftCheckbox, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox2.text")); // NOI18N
        shiftCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(altCheckbox, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox3.text")); // NOI18N
        altCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(metaCheckbox, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox4.text")); // NOI18N
        metaCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel13.setLabelFor(acceleratorText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jLabel13.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(controlCheckbox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(shiftCheckbox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(altCheckbox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(metaCheckbox))
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(jLabel13)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(acceleratorText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(clearAccelButton)))
                .addContainerGap(10, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(controlCheckbox)
                    .add(shiftCheckbox)
                    .add(altCheckbox)
                    .add(metaCheckbox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(acceleratorText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(clearAccelButton))
                .addContainerGap())
        );

        acceleratorText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jLabel13.ACSD")); // NOI18N
        clearAccelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.clearAccelButton.AccessibleContext.accessibleDescription")); // NOI18N
        controlCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox1.ACSD")); // NOI18N
        shiftCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox2.ACSD")); // NOI18N
        altCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox3.ACSD")); // NOI18N
        metaCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "jCheckBox4.ACSD")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(setIconButtonSmall, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jButton1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(setIconButtonLarge, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jButton2.text")); // NOI18N

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
                    .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(tooltipField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .add(textField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                            .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(setIconButtonSmall))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                            .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(setIconButtonLarge))))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(setIconButtonSmall)
                    .add(jLabel5)
                    .add(setIconButtonLarge))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        textField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel2.ACSD")); // NOI18N
        tooltipField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel4.ACSD")); // NOI18N
        iconButtonSmall.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jButton1.ACSD")); // NOI18N
        iconButtonLarge.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jButton2.ACSD")); // NOI18N
        setIconButtonSmall.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jButton1.ACSD")); // NOI18N
        setIconButtonLarge.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jButton2.ACSD")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jLabel6.setLabelFor(blockingDialogText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel6.text")); // NOI18N

        jLabel3.setLabelFor(blockingDialogTitle);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel3.text_1")); // NOI18N

        jLabel1.setLabelFor(blockingType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel1.text_1")); // NOI18N

        jLabel11.setLabelFor(selectedCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel11.text")); // NOI18N

        jLabel8.setLabelFor(enabledCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel8.text")); // NOI18N

        blockingType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Action", "Component", "Window", "Application" }));
        blockingType.setEnabled(false);
        blockingType.setOpaque(false);

        blockingDialogTitle.setEnabled(false);

        blockingDialogText.setEnabled(false);

        enabledCombo.setEditable(true);

        selectedCombo.setEditable(true);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(jLabel11)
                    .add(jLabel3)
                    .add(jLabel6)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(blockingDialogTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                    .add(blockingDialogText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                    .add(enabledCombo, 0, 163, Short.MAX_VALUE)
                    .add(selectedCombo, 0, 163, Short.MAX_VALUE)
                    .add(blockingType, 0, 163, Short.MAX_VALUE))
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
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(selectedCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                .addContainerGap(107, Short.MAX_VALUE))
        );

        blockingType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel1.ACSD")); // NOI18N
        blockingDialogTitle.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel3.ACSD")); // NOI18N
        blockingDialogText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel6.ACSD")); // NOI18N
        enabledCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel8.ACSD")); // NOI18N
        selectedCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel11.ACSD")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(actionToEdit, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.actionToEdit.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel12.text")); // NOI18N

        jLabel17.setLabelFor(jTabbedPane1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel17.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(backgroundTaskCheckbox, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.backgroundTaskCheckbox.text")); // NOI18N
        backgroundTaskCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.backgroundTaskCheckbox.toolTipText")); // NOI18N
        backgroundTaskCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        backgroundTaskCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundTaskCheckboxActionPerformed(evt);
            }
        });

        actionsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionsComboActionPerformed(evt);
            }
        });

        methodField.setColumns(24);

        org.openide.awt.Mnemonics.setLocalizedText(targetClassButton, org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.targetClassButton.text")); // NOI18N
        targetClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetClassButtonActionPerformed(evt);
            }
        });

        classField.setColumns(10);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(classField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scopeCombo, 0, 87, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(targetClassButton))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(targetClassButton)
                .add(classField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(scopeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        targetClassButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.targetClassButton.ACSD")); // NOI18N
        scopeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel9.ACSD")); // NOI18N
        classField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel9.ACSD")); // NOI18N

        actionsField.setColumns(10);
        actionsField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel12)
                    .add(actionToEdit)
                    .add(jLabel9)
                    .add(jLabel17))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(backgroundTaskCheckbox)
                    .add(layout.createSequentialGroup()
                        .add(actionsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(actionsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                    .add(methodField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(actionToEdit)
                    .add(actionsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(actionsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(methodField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(backgroundTaskCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTabbedPane1)
                    .add(jLabel17))
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jTabbedPane1.AccessibleContext.accessibleDescription")); // NOI18N
        actionsCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.actionsCombo.AccessibleContext.accessibleName")); // NOI18N
        actionsCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.actionsCombo.AccessibleContext.accessibleDescription")); // NOI18N
        methodField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.jLabel12.ACSD")); // NOI18N
        actionsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel.actionToEdit.ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

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
        updateState();
    }//GEN-LAST:event_actionsComboActionPerformed

    private void backgroundTaskCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundTaskCheckboxActionPerformed
        if(backgroundTaskCheckbox.isVisible()) {
            blockingType.setEnabled(backgroundTaskCheckbox.isSelected());
            blockingDialogText.setEnabled(backgroundTaskCheckbox.isSelected());
            blockingDialogTitle.setEnabled(backgroundTaskCheckbox.isSelected());
        }
    }//GEN-LAST:event_backgroundTaskCheckboxActionPerformed

    private void targetClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetClassButtonActionPerformed
        ClassPathFileChooser cp = new ClassPathFileChooser(sourceFile, new ClassPathFileChooser.Filter() {
            @Override
            public boolean accept(FileObject file) {
                return true;
            }
        },false,true);
        
        cp.getDialog(getLocalizedString("classChooserDialogTitle"), null).setVisible(true); // NOI18N
        if(cp.getSelectedFile() != null && cp.isConfirmed()) {
            selectedSourceFile = cp.getSelectedFile();
            String selectedClass = AppFrameworkSupport.getClassNameForFile(cp.getSelectedFile());
            if ((selectedClass == null) || (AppFrameworkSupport.getFileForClass(sourceFile, selectedClass) == null)) {
                //            classLabel.setText(""); // NOI18N
                classField.setText(""); // NOI18N
            } else {
                //            classLabel.setText(selectedClass);
                classField.setText(selectedClass);
            }
        }
    }//GEN-LAST:event_targetClassButtonActionPerformed

    
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
        act.setSelectedName((String) selectedCombo.getSelectedItem());
        act.setEnabledName((String) enabledCombo.getSelectedItem());
        
        act.putValue(Action.SMALL_ICON+".IconName",  smallIconName); // NOI18N
        act.putValue(LARGE_ICON_KEY+".IconName",  largeIconName); // NOTI18N
        return act;
    }
    
    private void setFromTextField(JTextField textField, ProxyAction act, String key) {
        String txt = textField.getText();
        if (txt != null) {
            txt = txt.trim();
            if (txt.equals("")) { // NOI18N
                txt = null;
            }
        }
        act.putValue(key, txt);
    }
    
    private String getSelectedClassname() {
        if(mode == Mode.NewActionForm) {
            ProxyAction.Scope scope = (Scope) scopeCombo.getSelectedItem();
            return scopeClasses.get(scope);
        }
        if(mode == Mode.NewActionGlobal) {
            return classField.getText();
        }
        return null;
    }
    
    ProxyAction getNewAction() {
        ProxyAction act = newAction;
        act.setClassname(getSelectedClassname());
        act.setId(methodField.getText());
        act.setMethodName(methodField.getText());
        act.putValue(Action.NAME,textField.getText());
        act.putValue(Action.SHORT_DESCRIPTION,tooltipField.getText());
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
        act.setSelectedName((String) selectedCombo.getSelectedItem());
        act.setEnabledName((String) enabledCombo.getSelectedItem());
        return act;
    }
    
    private ProxyAction.BlockingType getSelectedBlockingType() {
        return (ProxyAction.BlockingType)blockingType.getSelectedItem();
    }
    
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

        ProxyAction.Scope[] scopes;
        if (scopeClasses.containsKey(ProxyAction.Scope.Application)) {
            scopes = new ProxyAction.Scope[] { ProxyAction.Scope.Form, ProxyAction.Scope.Application };
            scopeCombo.setEnabled(true);
        } else {
            scopes = new ProxyAction.Scope[] { ProxyAction.Scope.Form };
            scopeCombo.setEnabled(false);
        }
        scopeCombo.setModel(new DefaultComboBoxModel(scopes));
    }
    
    private void setSelectedAction(ProxyAction act) {
        if(act != null) {
            if(globalMode) {
                globalAction = act;
            }
            setNewActionCreated(false);
            actionsCombo.setEnabled(true);
            //set the selection action by finding the right match
            for(int i=0; i<actionsCombo.getModel().getSize(); i++) {
                Object o = actionsCombo.getModel().getElementAt(i);
                if (o != null) {
                    ProxyAction act2 = (ProxyAction)o;
                    if(act2.getId().equals(act.getId())) {
                        actionsCombo.setSelectedItem(act2);
                        actionsField.setText(act2.getId());
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
    private javax.swing.JTextField actionsField;
    public javax.swing.JCheckBox altCheckbox;
    private javax.swing.JCheckBox backgroundTaskCheckbox;
    private javax.swing.JTextField blockingDialogText;
    private javax.swing.JTextField blockingDialogTitle;
    private javax.swing.JComboBox blockingType;
    private javax.swing.JTextField classField;
    private javax.swing.JButton clearAccelButton;
    public javax.swing.JCheckBox controlCheckbox;
    private javax.swing.JComboBox enabledCombo;
    private javax.swing.JButton iconButtonLarge;
    private javax.swing.JButton iconButtonSmall;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JCheckBox metaCheckbox;
    private javax.swing.JTextField methodField;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JComboBox selectedCombo;
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
        textField.setText(""); // NOI18N
        tooltipField.setEnabled(false);
        tooltipField.setText(""); // NOI18N
        acceleratorListener.setEnabled(false);
        setAcceleratorPanelEnabled(false);
        clearAcceleratorPanel();
        iconButtonLarge.setEnabled(false);
        iconButtonLarge.setIcon(null);
        setIconButtonLarge.setEnabled(false);
        iconButtonSmall.setEnabled(false);
        iconButtonSmall.setIcon(null);
        setIconButtonSmall.setEnabled(false);
        selectedCombo.setEnabled(false);
        enabledCombo.setEnabled(false);
        
        blockingDialogText.setEnabled(false);
        blockingDialogText.setText(""); // NOI18N
        blockingDialogTitle.setEnabled(false);
        blockingDialogTitle.setText(""); // NOI18N
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
        selectedCombo.setEnabled(true);
        enabledCombo.setEnabled(true);
        actionsCombo.setEnabled(false);
        textField.setText(""); // NOI18N
        acceleratorListener.clearFields();
        tooltipField.setText(""); // NOI18N
        newMethodName = ""; // NOI18N
        iconButtonSmall.setIcon(null);
        iconButtonLarge.setIcon(null);
        // josh: is this next line correct?
        blockingType.setEnabled(false);
        
        classField.setText(""); // NOI18N
        classField.setVisible(false);
        
        methodField.setText(""); // NOI18N
        methodField.setEditable(true);
        
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
        if(classname.trim().equals("")) { return false; } // NOI18N
        
        ActionManager am = ActionManager.getActionManager(sourceFile);
        if(am.getFileForClass(classname) == null) { return false; }
        return true;
    }
    
    boolean isDuplicateMethod() {
        newMethodName = methodField.getText();
        String classname = getSelectedClassname();
        if(classname == null) { return true; }
        if(classname.trim().equals("")) { return true; } // NOI18N
        
        
        ActionManager am = ActionManager.getActionManager(sourceFile);
        if(am.isExistingMethod(classname, newMethodName)) {
            return true;
        }
        return false;
    }
    
    
    
    boolean canCreateNewAction() {
        newMethodName = methodField.getText();
        
        if(!isMethodNonEmpty()) { 
            return false;
        }
        
        if(doesMethodContainBadChars()) {
            return false;
        }
        
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
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
        
        @Override
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
        
        @Override
        public void actionPerformed(ActionEvent e) {
            ProxyAction action = getSelectedAction();
            IconEditor iconEditor = new IconEditor();
            iconEditor.setExternalIconsAllowed(false);
            // if this is a dir, then use the application class instead
            if(sourceFile.isFolder()) {
                sourceFile = ActionManager.getActionManager(sourceFile).getApplicationClassFile();
            }
            iconEditor.setSourceFile(sourceFile);
            
            if(Action.SMALL_ICON.equals(iconKey)){
                if (smallIconName != null) {
                    iconEditor.setAsText(removeInitialSlash(smallIconName));
                }
            } else {
                if (largeIconName != null) {
                    iconEditor.setAsText(removeInitialSlash(largeIconName));
                }
            }
            DialogDescriptor dd = new DialogDescriptor(iconEditor.getCustomEditor(), NbBundle.getMessage(ActionPropertyEditorPanel.class, "CTL_SelectIcon_Title"));
            if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                IconEditor.NbImageIcon nbIcon = (IconEditor.NbImageIcon) iconEditor.getValue();
                Icon icon = nbIcon != null ? nbIcon.getIcon() : null;
                iconButton.setIcon(icon);
                iconButton.setText(icon == null ? "..." : null); // NOI18N
                action.putValue(iconKey, icon);
                String iconName = nbIcon != null ? nbIcon.getName() : null;
                if (iconName != null && !iconName.startsWith("/")) { // NOI18N
                    iconName = "/"+iconName; // NOI18N
                }
                if(Action.SMALL_ICON.equals(iconKey)) {
                    smallIconName = iconName;
                } else {
                    largeIconName = iconName;
                }
                action.putValue(iconKey+".IconName", iconName); // NOI18N
                actionPropertiesUpdated = true;
            }
        }
    }
    
    private String removeInitialSlash(String name) {
        if ((name != null) && name.startsWith("/")) { // NOI18N
            name = name.substring(1);
        }
        return name;
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
    
    private String getLocalizedString(String key) {
        return NbBundle.getMessage(ActionPropertyEditorPanel.class, "ActionPropertyEditorPanel."+key); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    private PropertyEnv env;
    protected void attachEnv(PropertyEnv env) {
        this.env = env;
    }
            
}
