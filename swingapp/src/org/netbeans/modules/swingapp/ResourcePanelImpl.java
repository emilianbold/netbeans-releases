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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.View;
import org.netbeans.modules.form.ResourcePanel;
import org.netbeans.modules.form.ResourceValue;
import org.openide.util.NbBundle;

/**
 * A panel used in custom property editors to allow to define the property
 * value as a resource.
 * @see org.netbeans.modules.form.ResourceWrapperEditor
 * 
 * @author Tomas Pavek
 */
class ResourcePanelImpl extends javax.swing.JPanel implements ResourcePanel {

    private DesignResourceMap resources;
    private Class valueType;
    private ResourceValueImpl resourceValue;
    private boolean validValue;
    private ResourceValueImpl lastValue;
    private String newKey;
    private List<ChangeListener> listeners;
    private boolean ignoreCombo;

    ResourcePanelImpl(DesignResourceMap resMap, Class valueType) {
        this.resources = resMap;
        this.valueType = valueType;
        initComponents();
        invalidLabel.setVisible(false);
        // prevent whole property window from closing when Enter is pressed in
        // the combo editor; select the typed key instead
        keyCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    keyComboActionPerformed(null);
                }
            }
        });
        classRadio.setSelected(true);
    }

    public void init(String key, boolean enable) {
        lastValue = null;

        ResourceValueImpl resVal = resources.getResourceValue(key, valueType);
        if (resVal == null) {
            resVal = new ResourceValueImpl(key, valueType, null, null, null,
                    valueType==String.class, getStorageLevel(), resources.getSourceFile());
            newKey = key;
        }
        else newKey = null;

        if (enable) {
            resourceValue = resVal;
            loadKeys();
            resourceCheckBox.setSelected(true);
            fireChange(); // fire change even if the key existed - so the property editor receives a new instance
        }
        else {
            resourceValue = null;
            if (newKey == null) {
                resVal.setValue(null);
                resVal.setStringValue(null);
            }
            lastValue = resVal;
            resourceCheckBox.setSelected(false);
        }
        enableControls();
        updateControls();
    }

    public void setValue(Object value, String stringValue, String cpResourceName) {
        if (!isEditingEnabled())
            return;

        DesignResourceMap resMap = resources.getLevel(getStorageLevel());
        if (cpResourceName != null) {
            String pkgResName = resMap.getResourcesDir();
            if (cpResourceName.startsWith(pkgResName)) { // relative resource name
                stringValue = cpResourceName.substring(pkgResName.length());
            } else { // full resource name, for app framework need starting /
                stringValue = (cpResourceName.startsWith("/") ? "" : "/") + cpResourceName; // NOI18N
            }
        }
        if (stringValue != null) {
            Object valueFromString;
            try {
                valueFromString = resMap.evaluateStringValue(stringValue, valueType);
                if (value == null) {
                    value = valueFromString;
                }
                if (cpResourceName == null && resourceValue.getClassPathResourceName() != null) {
                    cpResourceName = resMap.getResourcesDir() + stringValue; // [it's a hack - stringValue may not be usable this way]
                }
            } catch (org.jdesktop.application.ResourceMap.LookupException ex) { // don't understand it
                value = ResourceValue.IGNORED_VALUE;
//                stringValue = null;
                cpResourceName = null;
            }
        } else {
            stringValue = ResourceUtils.getValueAsString(value);
            if (stringValue == null) {
                value = ResourceValue.IGNORED_VALUE;
                stringValue = ""; // NOI18N
            }
//                return; // [or throw something?]
        }
        resourceValue.setStringValue(stringValue);
        if (cpResourceName != null) {
            resourceValue.setClassPathResourceName(cpResourceName);
        }
        resourceValue.setValue(value);
        setValueFieldText(stringValue);
        setValidValue(value != ResourceValue.IGNORED_VALUE);
    }

    public ResourceValue getResource() {
        return resourceValue;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        if (listeners == null)
            listeners = new LinkedList<ChangeListener>();
        else
            listeners.remove(l);
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    public JComponent getComponent() {
        return this;
    }

    // -----

    private boolean isEditingEnabled() {
        return resourceValue != null;
    }

    private void setStorageLevel(int level) {
        switch (level) {
            case DesignResourceMap.CLASS_LEVEL:
                classRadio.setSelected(true);
                break;
            case DesignResourceMap.PACKAGE_LEVEL:
                packageRadio.setSelected(true);
                break;
            case DesignResourceMap.APP_LEVEL:
                applicationRadio.setSelected(true);
                break;
        }
    }

    private int getStorageLevel() {
        if (applicationRadio.isSelected())
            return DesignResourceMap.APP_LEVEL;
        else if (packageRadio.isSelected())
            return DesignResourceMap.PACKAGE_LEVEL;
        else
            return DesignResourceMap.CLASS_LEVEL;
    }

    private void loadKeys() {
        DesignResourceMap resMap = resources.getLevel(resourceValue.getStorageLevel());
        Collection<String> keys = resMap.collectKeys("\\S+", false); // NOI18N
        List<String> keyList = new ArrayList<String>(keys.size()+2);
        keyList.addAll(keys);
        String actualKey = resourceValue.getKey();
        if (!keyList.contains(actualKey))
            keyList.add(actualKey);
        if (newKey != null && !newKey.equals(actualKey) && !keyList.contains(newKey))
            keyList.add(newKey);
        Collections.sort(keyList);

        keyCombo.setModel(new DefaultComboBoxModel(keyList.toArray()));
    }

    private void updateControls() {
        if (isEditingEnabled()) {
            if (resourceValue != null) {
                i18nCheckBox.setSelected(resourceValue.isInternationalized());
                ignoreCombo = true;
                keyCombo.setSelectedItem(resourceValue.getKey());
                ignoreCombo = false;
                String strValue = resourceValue.getStringValue();
                setValueFieldText(strValue != null ? strValue : ""); // NOI18N
                setStorageLevel(resourceValue.getStorageLevel());
            }
        }
        else {
            setValueFieldText(""); // NOI18N
            if (lastValue != null) {
                i18nCheckBox.setSelected(lastValue.isInternationalized());
                ignoreCombo = true;
                keyCombo.setSelectedItem(lastValue.getKey());
                ignoreCombo = false;
                setStorageLevel(lastValue.getStorageLevel());
            }
        }
    }

    private void enableControls() {
        boolean enabled = isEditingEnabled();
        i18nCheckBox.setEnabled(enabled);
        classRadio.setEnabled(enabled);
        packageRadio.setEnabled(enabled);
        applicationRadio.setEnabled(enabled);
        keyCombo.setEnabled(enabled);
        valueTextField.setEnabled(enabled);
    }

    // called when the panel is enabled/disabled via checkbox (by the user)
    private void enablePanel(boolean enable) {
        if (enable && !isEditingEnabled()) {
            // enable
            assert lastValue != null; // suppose setKey was called at least
            resourceValue = lastValue;
            loadKeys();
        }
        else if (!enable && isEditingEnabled()) {
            // disable; remember everything except the value which will be renewed when re-enabled
            lastValue = new ResourceValueImpl(resourceValue);
            lastValue.setValue(null);
            lastValue.setStringValue(null);
            resourceValue = null;
        }
        enableControls();
        updateControls();
    }

    private void setValidValue(boolean valid) {
        validValue = valid;
        invalidLabel.setVisible(!valid);
        valueTextField.setToolTipText(valid ? null : invalidLabel.getToolTipText());
        if (!valid) {
            valueTextField.setSelectionStart(0);
            valueTextField.setSelectionEnd(valueTextField.getDocument().getLength());
        }
    }

    private boolean isValidValue() {
        return validValue;
    }

    private void setValueFieldText(String str) {
        // encode multiline text
        StringBuilder buf = null;
        for (int i=0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' || c == '\n') {
                if (buf == null) {
                    buf = new StringBuilder(str.length() * 2);
                    buf.append(str.substring(0, i));
                }
                if (c == '\\') { // encode backslash
                    buf.append("\\\\"); // NOI18N
                } else { // c == '\n' // encode new line
                    buf.append("\\n"); // NOI18N
                }
            } else if (buf != null) {
                buf.append(c);
            }
        }
        valueTextField.setText(buf != null ? buf.toString() : str);
    }

    private String getValueFieldText() {
        // decode backslash and new line
        String str = valueTextField.getText();
        StringBuilder buf = null;
        boolean backslash = false;
        for (int i=0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\') {
                if (buf == null) {
                    buf = new StringBuilder(str.length());
                    buf.append(str.substring(0, i));
                }
                if (backslash) { // second backslash - convert to one
                    buf.append('\\');
                    backslash = false;
                } else {
                    backslash = true;
                }
            } else {
                if (backslash) {
                    if (c == 'n') { // new line
                        c = '\n';
                    } else { // ignore other backslash combinations
                        buf.append('\\');
                    }
                    backslash = false;
                }
                if (buf != null) {
                    buf.append(c);
                }
            }
        }
        return buf != null ? buf.toString() : str;
    }

    private void setStorageLevelToValue(int level) {
        if (resourceValue != null) {
            resourceValue.setStorageLevel(level);
            if (resourceValue.getClassPathResourceName() != null) {
                // changing the package - should re-evaluate the string value
                // [it's a hack - the string value need not be short resource name]
                Object oldVal = resourceValue.getValue();
                String text = getValueFieldText();
                setValue(null, text, null);
                if (resourceValue.getValue() == oldVal) { // the textfield's text is invalid
                    String strValue = resourceValue.getStringValue();
                    if (!text.equals(strValue)) // but something else is in resourceValue
                        setValue(null, strValue, null);
                }
                if (resourceValue.getValue() != oldVal) {
                    fireChange();
                }
                else { // both strings are invalid, let's use the one from the textfield
                    resourceValue.setStringValue(text);
                    // at least update the package
                    String resDir = resources.getLevel(level).getResourcesDir();
                    resourceValue.setClassPathResourceName(resDir + text);
                }
            }
        }
    }

    private void fireChange() {
        List<ChangeListener> list;
        synchronized (this) {
            if (listeners == null)
                return;
            list = new ArrayList<ChangeListener>(listeners);
        }
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : list) {
            l.stateChanged(e);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jSeparator1 = new javax.swing.JSeparator();
        resourceCheckBox = new javax.swing.JCheckBox();
        i18nCheckBox = new javax.swing.JCheckBox();
        levelLabel = new javax.swing.JLabel();
        keyLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        classRadio = new javax.swing.JRadioButton();
        packageRadio = new javax.swing.JRadioButton();
        applicationRadio = new javax.swing.JRadioButton();
        valueTextField = new javax.swing.JTextField();
        keyCombo = new javax.swing.JComboBox();
        invalidLabel = new javax.swing.JLabel();
        layoutBugWorkaroundLabel = new javax.swing.JLabel();
        hintLabel = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        org.openide.awt.Mnemonics.setLocalizedText(resourceCheckBox, org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.resourceCheckBox.text")); // NOI18N
        resourceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        resourceCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        resourceCheckBox.addActionListener(formListener);

        i18nCheckBox.setText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.i18nCheckBox.text")); // NOI18N
        i18nCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        i18nCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        i18nCheckBox.addActionListener(formListener);

        levelLabel.setText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.levelLabel.text")); // NOI18N

        keyLabel.setText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.keyLabel.text")); // NOI18N

        valueLabel.setText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.valueLabel.text")); // NOI18N

        buttonGroup1.add(classRadio);
        classRadio.setText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.classRadio.text")); // NOI18N
        classRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        classRadio.addActionListener(formListener);

        buttonGroup1.add(packageRadio);
        packageRadio.setText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.packageRadio.text")); // NOI18N
        packageRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        packageRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        packageRadio.addActionListener(formListener);

        buttonGroup1.add(applicationRadio);
        applicationRadio.setText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.applicationRadio.text")); // NOI18N
        applicationRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        applicationRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        applicationRadio.addActionListener(formListener);

        valueTextField.addActionListener(formListener);

        keyCombo.setEditable(true);
        keyCombo.addActionListener(formListener);

        invalidLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/swingapp/resources/invalid.gif")));
        invalidLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl.invalidLabel.toolTipText")); // NOI18N

        hintLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/swingapp/resources/help.gif")));
        hintLabel.addMouseListener(formListener);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(keyLabel)
                    .add(valueLabel)
                    .add(levelLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(classRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(packageRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(applicationRadio))
                    .add(keyCombo, 0, 270, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(valueTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(invalidLabel)
                        .add(0, 0, 0)
                        .add(layoutBugWorkaroundLabel)))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(resourceCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hintLabel)
                .add(15, 15, 15)
                .add(i18nCheckBox)
                .addContainerGap(102, Short.MAX_VALUE))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resourceCheckBox)
                    .add(i18nCheckBox)
                    .add(hintLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyLabel)
                    .add(keyCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(valueLabel)
                    .add(valueTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(invalidLabel)
                    .add(layoutBugWorkaroundLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(classRadio)
                    .add(packageRadio)
                    .add(applicationRadio)
                    .add(levelLabel))
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.MouseListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == resourceCheckBox) {
                ResourcePanelImpl.this.resourceCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == i18nCheckBox) {
                ResourcePanelImpl.this.i18nCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == classRadio) {
                ResourcePanelImpl.this.classRadioActionPerformed(evt);
            }
            else if (evt.getSource() == packageRadio) {
                ResourcePanelImpl.this.packageRadioActionPerformed(evt);
            }
            else if (evt.getSource() == applicationRadio) {
                ResourcePanelImpl.this.applicationRadioActionPerformed(evt);
            }
            else if (evt.getSource() == valueTextField) {
                ResourcePanelImpl.this.valueTextFieldActionPerformed(evt);
            }
            else if (evt.getSource() == keyCombo) {
                ResourcePanelImpl.this.keyComboActionPerformed(evt);
            }
        }

        public void mouseClicked(java.awt.event.MouseEvent evt) {
        }

        public void mouseEntered(java.awt.event.MouseEvent evt) {
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
        }

        public void mousePressed(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == hintLabel) {
                ResourcePanelImpl.this.hintLabelMousePressed(evt);
            }
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void hintLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hintLabelMousePressed
        // show a permanent tooltip (dismiss on mouse click, key press or focus lost)
        JToolTip tooltip = new JToolTip();
        tooltip.setTipText(NbBundle.getMessage(ResourcePanelImpl.class, "ResourcePanelImpl_ToolTip")); // NOI18N
        Dimension size = getSize(); // current panel size
        if (tooltip.getPreferredSize().width > size.width) { // force the tooltip to wrap text
            View v = (View) tooltip.getClientProperty("html"); // NOI18N
            if (v != null) {
                v.setSize(size.width, 0);
            }
        }
        final Window win = new Window(SwingUtilities.getWindowAncestor(this));
        win.setFocusable(true);
        win.add(tooltip);
        win.pack();
        Point p = new Point(-3, -win.getHeight() - 3);
        SwingUtilities.convertPointToScreen(p, this);
        win.setLocation(p.x, p.y);
        win.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ev) {
                win.setVisible(false);
                win.dispose();
                resourceCheckBox.requestFocusInWindow();
            }
        });
        win.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent ev) {
                win.setVisible(false);
                win.dispose();
            }
        });
        tooltip.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                win.setVisible(false);
                win.dispose();
                resourceCheckBox.requestFocusInWindow();
            }
        });
        win.setVisible(true);
    }//GEN-LAST:event_hintLabelMousePressed

    private void applicationRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applicationRadioActionPerformed
        setStorageLevelToValue(DesignResourceMap.APP_LEVEL);
        loadKeys();
        updateControls();
    }//GEN-LAST:event_applicationRadioActionPerformed

    private void packageRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageRadioActionPerformed
        setStorageLevelToValue(DesignResourceMap.PACKAGE_LEVEL);
        loadKeys();
        updateControls();
    }//GEN-LAST:event_packageRadioActionPerformed

    private void classRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classRadioActionPerformed
        setStorageLevelToValue(DesignResourceMap.CLASS_LEVEL);
        loadKeys();
        updateControls();
    }//GEN-LAST:event_classRadioActionPerformed

    private void resourceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resourceCheckBoxActionPerformed
        enablePanel(resourceCheckBox.isSelected());
        fireChange();
    }//GEN-LAST:event_resourceCheckBoxActionPerformed

    private void i18nCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_i18nCheckBoxActionPerformed
        resourceValue.setInternationalized(i18nCheckBox.isSelected());
    }//GEN-LAST:event_i18nCheckBoxActionPerformed

    private void valueTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueTextFieldActionPerformed
        setValue(null, getValueFieldText(), null);
        if (resourceValue != null && resourceValue.getValue() != null) {
            if (isValidValue()) {
                fireChange();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }//GEN-LAST:event_valueTextFieldActionPerformed

    private void keyComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyComboActionPerformed
        if (ignoreCombo)
            return;
        String key = (String) keyCombo.getEditor().getItem();
        if (resourceValue.getKey().equals(key))
            return;

        ResourceValueImpl resVal = resources.getResourceValue(key, valueType);
        if (resVal == null) {
            resVal = new ResourceValueImpl(resourceValue);
            resVal.setKey(key);
            resourceValue = resVal;
            loadKeys(); // to include the new key
        }
        else {
            resourceValue = resVal;
            fireChange(); // to let the property editor get the new value
        }
        updateControls();
    }//GEN-LAST:event_keyComboActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton applicationRadio;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton classRadio;
    private javax.swing.JLabel hintLabel;
    private javax.swing.JCheckBox i18nCheckBox;
    private javax.swing.JLabel invalidLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox keyCombo;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JLabel layoutBugWorkaroundLabel;
    private javax.swing.JLabel levelLabel;
    private javax.swing.JRadioButton packageRadio;
    private javax.swing.JCheckBox resourceCheckBox;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueTextField;
    // End of variables declaration//GEN-END:variables

}
