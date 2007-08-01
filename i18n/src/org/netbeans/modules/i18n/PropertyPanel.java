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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;


/**
 * Panel which is used for customizing key-value pair (and comment also)
 * encapsulated by {@code I18nString} object.
 * It's used inside {@code I18nPanel}.
 *
 * @author  Peter Zavadsky
 * @see I18nString
 * @see I18nPanel
 */
public class PropertyPanel extends JPanel {
    
    /** property representing the I18String. Change is fired when the i18string changes.
     * Old and new objects are not sent with the notification.
     */
    public static final String PROP_STRING = "propString";              //NOI18N

    /** Name for resource property. */
    public static final String PROP_RESOURCE = "property_resource";     //NOI18N
    
    /** Helper name for dummy action command. */
    private static final String DUMMY_ACTION = "dont_proceed";          //NOI18N
    
    /** Customized <code>I18nString</code>. */
    protected I18nString i18nString;

    /** the file for that resource should be selected **/
    private FileObject file;

    /** Internal flag to block handling of changes to the key jtextfield,
     * which didn't originate from the user but from the code. If this is >0, 
     * values are just being pushed to the UI, if <=0, values are being received
     * from the ui.
     **/
    private int internalTextChange = 0;    
    
    
    /** Creates new <code>PropertyPanel</code>. */
    public PropertyPanel() {
        initComponents();
        myInitComponents();
        initAccessibility();
    }

    @Override
    public void setEnabled(boolean ena) {
        super.setEnabled(ena);
        commentText.setEnabled(ena);
        commentLabel.setEnabled(ena);
        commentScroll.setEnabled(ena);
        
        keyBundleCombo.setEnabled(ena);
        keyLabel.setEnabled(ena);
        
        replaceFormatButton.setEnabled(ena);
        replaceFormatLabel.setEnabled(ena);
        replaceFormatTextField.setEnabled(ena);
        
        valueLabel.setEnabled(ena); 
        valueText.setEnabled(ena);
        valueScroll.setEnabled(ena);
    }
    
    /** Seter for <code>i18nString</code> property. */
    public void setI18nString(I18nString i18nString) {
        this.i18nString = i18nString;
        
        updateAllValues();
        firePropertyChange(PROP_STRING, null,null);
    }

    /** Sets the file for that resource should be selected **/
    public void setFile(FileObject fo) {
        this.file = fo;
    }

    public FileObject getFile() {
        return file;
    }

    /** Initializes UI values. */
    void updateAllValues() {
        resourceText.setText(getResourceName(i18nString.getSupport().getResourceHolder().getResource()));
        updateBundleKeys();        
        updateKey();
        updateValue();
        updateComment();
    } 
    
    /** Updates selected item of <code>keyBundleCombo</code> UI. 
     */
    private void updateKey() {       
        String key = i18nString.getKey();
        
        if ((key == null) || !key.equals(keyBundleCombo.getSelectedItem())) {
            // Trick to avoid firing key selected property change.
            String oldActionCommand = keyBundleCombo.getActionCommand();
            keyBundleCombo.setActionCommand(DUMMY_ACTION);

            internalTextChange++;
            keyBundleCombo.setSelectedItem((key != null) ? key : "");   //NOI18N
            internalTextChange--;
            
            keyBundleCombo.setActionCommand(oldActionCommand);
        }
        
        updateReplaceText();
    }

    /** Updates <code>valueText</code> UI. 
     */
    private void updateValue() {            
        String value = i18nString.getValue();
        
        if (!valueText.getText().equals(value)) {
            valueText.setText((value != null) ? value : "");            //NOI18N
        }
       
       updateReplaceText();            
    }
    
    /** Updates <code>commentText</code> UI. */
    private void updateComment() {
        String comment = i18nString.getComment();
        
        if (!commentText.getText().equals(comment)) {
            commentText.setText((comment != null) ? comment : "");      //NOI18N
        }
    }
    
    /** Updates <code>replaceFormatTextField</code>. */
    protected void updateReplaceText() {
        replaceFormatTextField.setText(i18nString.getReplaceString());
    }
    
    /** Updates <code>keyBundleCombo</code> UI. */
    void updateBundleKeys() {
        // Trick to avoid firing key selected property change.
        String oldActionCommand = keyBundleCombo.getActionCommand();
        keyBundleCombo.setActionCommand(DUMMY_ACTION);

        internalTextChange++;
        String[] keys = i18nString.getSupport().getResourceHolder().getAllKeys();
        Arrays.sort(keys);
        keyBundleCombo.setModel(new DefaultComboBoxModel(keys));
        internalTextChange--;
        
        keyBundleCombo.setActionCommand(oldActionCommand);
        
        updateKey();
    }
    
     /** Helper method. Changes resource. */
    private void changeResource(DataObject resource) {
        if (resource == null) {
            throw new IllegalArgumentException();
        }

        DataObject oldValue = i18nString.getSupport().getResourceHolder().getResource();
        
        if ((oldValue != null) && oldValue.equals(resource)) {
            return;
        }
        
        i18nString.getSupport().getResourceHolder().setResource(resource);
        String newResourceValue = i18nString.getSupport().getResourceHolder()
                                  .getValueForKey(i18nString.getKey());
        if (newResourceValue != null) {
            i18nString.setValue(newResourceValue);
        }
        updateAllValues();

        firePropertyChange(PROP_RESOURCE, oldValue, resource);

        I18nUtil.getOptions().setLastResource2(resource);
    }

    public void setResource(DataObject resource) {
        if (isResourceClass(resource.getClass())) {
            changeResource(resource);
        }
    }        

    private boolean isResourceClass(Class clazz) {
        return Arrays.asList(
                i18nString.getSupport().getResourceHolder().getResourceClasses()).contains(clazz);
    }

    private String getResourceName(DataObject resource) {
        if (resource == null) {
            return "";                                                  //NOI18N
        } else {
            String name = Util.getResourceName(file, resource.getPrimaryFile(), '.', false);
            return (name != null) ? name : "";                          //NOI18N
        }
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_PropertyPanel"));   //NOI18N
        valueText.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_valueText"));       //NOI18N
        commentText.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_commentText"));     //NOI18N
        replaceFormatButton.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_CTL_Format"));      //NOI18N
        replaceFormatTextField.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_replaceFormatTextField"));//NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_CTL_BrowseButton"));//NOI18N
        resourceText.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_ResourceText"));    //NOI18N
    }
    
    private void myInitComponents() {
        argumentsButton.setVisible(false);
        // hook the Key combobox edit-field for changes
        ((JTextField) keyBundleCombo.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(new DocumentListener() {              
                    public void changedUpdate(DocumentEvent e) { keyBundleTextChanged();}
                    public void insertUpdate(DocumentEvent e) {keyBundleTextChanged();}
                    public void removeUpdate(DocumentEvent e) {keyBundleTextChanged();}
                }
               );
        valueText.getDocument().addDocumentListener(new DocumentListener() {              
                    public void changedUpdate(DocumentEvent e) { valueTextChanged();}
                    public void insertUpdate(DocumentEvent e) {valueTextChanged();}
                    public void removeUpdate(DocumentEvent e) {valueTextChanged();}
                }
               );

    }
    
    private void keyBundleTextChanged() {
        if (internalTextChange == 0) {
            String key = ((JTextField) keyBundleCombo.getEditor().getEditorComponent()).getText();

            if (!key.equals(i18nString.getKey())) {        
                i18nString.setKey(key);
                firePropertyChange(PROP_STRING, null, null);
            } 
        }
    }

    private void valueTextChanged() {
        i18nString.setValue(valueText.getText());
//        updateValue();
        firePropertyChange(PROP_STRING, null, null);                
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        commentLabel = new javax.swing.JLabel();
        commentScroll = new javax.swing.JScrollPane();
        commentText = new javax.swing.JTextArea();
        keyLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        valueScroll = new javax.swing.JScrollPane();
        valueText = new javax.swing.JTextArea();
        keyBundleCombo = new javax.swing.JComboBox();
        replaceFormatTextField = new javax.swing.JTextField();
        replaceFormatLabel = new javax.swing.JLabel();
        replaceFormatButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        resourceText = new javax.swing.JTextField();
        argumentsButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();

        commentLabel.setLabelFor(commentText);
        org.openide.awt.Mnemonics.setLocalizedText(commentLabel, I18nUtil.getBundle().getString("LBL_Comment")); // NOI18N

        commentText.setColumns(40);
        commentText.setRows(2);
        commentText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commentTextFocusLost(evt);
            }
        });
        commentScroll.setViewportView(commentText);

        keyLabel.setLabelFor(keyBundleCombo);
        org.openide.awt.Mnemonics.setLocalizedText(keyLabel, I18nUtil.getBundle().getString("LBL_Key")); // NOI18N

        valueLabel.setLabelFor(valueText);
        org.openide.awt.Mnemonics.setLocalizedText(valueLabel, I18nUtil.getBundle().getString("LBL_Value")); // NOI18N

        valueText.setColumns(40);
        valueText.setRows(2);
        valueText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueTextFocusLost(evt);
            }
        });
        valueScroll.setViewportView(valueText);

        keyBundleCombo.setEditable(true);
        keyBundleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyBundleComboActionPerformed(evt);
            }
        });

        replaceFormatTextField.setColumns(40);
        replaceFormatTextField.setEditable(false);
        replaceFormatTextField.selectAll();
        replaceFormatTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                replaceFormatTextFieldFocusGained(evt);
            }
        });

        replaceFormatLabel.setLabelFor(replaceFormatTextField);
        org.openide.awt.Mnemonics.setLocalizedText(replaceFormatLabel, I18nUtil.getBundle().getString("LBL_ReplaceFormat")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(replaceFormatButton, I18nUtil.getBundle().getString("CTL_Format")); // NOI18N
        replaceFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceFormatButtonActionPerformed(evt);
            }
        });

        jLabel1.setLabelFor(resourceText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, I18nUtil.getBundle().getString("LBL_BundleName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(argumentsButton, I18nUtil.getBundle().getString("CTL_Arguments")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, I18nUtil.getBundle().getString("CTL_BrowseButton")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(valueLabel)
                            .add(commentLabel)
                            .add(keyLabel)
                            .add(replaceFormatLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(resourceText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseButton))
                            .add(keyBundleCombo, 0, 414, Short.MAX_VALUE)
                            .add(valueScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                            .add(commentScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                            .add(replaceFormatTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(argumentsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(replaceFormatButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(resourceText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyLabel)
                    .add(keyBundleCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(valueLabel)
                    .add(valueScroll))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(commentLabel)
                    .add(commentScroll))
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(replaceFormatTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(replaceFormatLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(replaceFormatButton)
                    .add(argumentsButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        ResourceHolder rh = i18nString.getSupport().getResourceHolder();
        DataObject template;
        try {
            template = rh.getTemplate(rh.getResourceClasses()[0]);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        DataObject resource = SelectorUtils.selectOrCreateBundle(file, template);
//      DataObject resource = SelectorUtils.selectBundle(this.project, file);
        if (resource != null) {
	    changeResource(resource);
        }

    }//GEN-LAST:event_browseButtonActionPerformed

    private void replaceFormatTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_replaceFormatTextFieldFocusGained
        // Accessibility
        replaceFormatTextField.selectAll();
    }//GEN-LAST:event_replaceFormatTextFieldFocusGained

    private void replaceFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceFormatButtonActionPerformed
        final Dialog[] dialogs = new Dialog[1];
        final HelpStringCustomEditor customPanel = new HelpStringCustomEditor(
                                                        i18nString.getReplaceFormat(),
                                                        I18nUtil.getReplaceFormatItems(),
                                                        I18nUtil.getReplaceHelpItems(),
                                                        I18nUtil.getBundle().getString("LBL_ReplaceCodeFormat"),
                                                        I18nUtil.PE_REPLACE_CODE_HELP_ID);

        DialogDescriptor dd = new DialogDescriptor(
            customPanel,
            I18nUtil.getBundle().getString("LBL_ReplaceStringFormatEditor"),//NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    final Object source = ev.getSource();
                    if (source == DialogDescriptor.OK_OPTION) {
                        String newText = (String) customPanel.getPropertyValue();
                        
                        if (!newText.equals(replaceFormatTextField.getText())) {
                            i18nString.setReplaceFormat(newText);                            
                            updateReplaceText();
                            firePropertyChange(PROP_STRING, null, null);
                            
                            // Reset option as well.
                            I18nUtil.getOptions().setReplaceJavaCode(newText);
                        }
                        
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else if (source == DialogDescriptor.CANCEL_OPTION) {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }
                       });
                       dialogs[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialogs[0].setVisible(true);
    }//GEN-LAST:event_replaceFormatButtonActionPerformed

    private void keyBundleComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyBundleComboActionPerformed
        if (DUMMY_ACTION.equals(evt.getActionCommand())) {
            return;
        }

        String key = (String)keyBundleCombo.getSelectedItem();
        i18nString.setKey(key);
        updateKey();
        
        String value = i18nString.getSupport().getResourceHolder().getValueForKey(key);
        if (value != null) {
            i18nString.setValue(value);
            updateValue();
        }
        
        String comment = i18nString.getSupport().getResourceHolder().getCommentForKey(key);
        if (comment != null) {
            i18nString.setComment(comment);
            updateComment();
        }
        firePropertyChange(PROP_STRING, null, null);
    }//GEN-LAST:event_keyBundleComboActionPerformed

    private void commentTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextFocusLost
        i18nString.setComment(commentText.getText());
        updateComment();
        firePropertyChange(PROP_STRING, null, null);        
    }//GEN-LAST:event_commentTextFocusLost

    private void valueTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueTextFocusLost
        valueTextChanged();
    }//GEN-LAST:event_valueTextFocusLost
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton argumentsButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JScrollPane commentScroll;
    private javax.swing.JTextArea commentText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox keyBundleCombo;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JButton replaceFormatButton;
    private javax.swing.JLabel replaceFormatLabel;
    private javax.swing.JTextField replaceFormatTextField;
    private javax.swing.JTextField resourceText;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JScrollPane valueScroll;
    private javax.swing.JTextArea valueText;
    // End of variables declaration//GEN-END:variables
        
}
