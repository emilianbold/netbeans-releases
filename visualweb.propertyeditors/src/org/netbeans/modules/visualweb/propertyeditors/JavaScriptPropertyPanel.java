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
package org.netbeans.modules.visualweb.propertyeditors;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.util.ResourceBundle;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * A custom property editor for JavaScript, that displays code using basic
 * syntax highlighting.
 *
 * @author eric
 * @author gjmurphy
 */
public class JavaScriptPropertyPanel extends PropertyPanelBase {

    private static String instructions =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString(
                "JavaScriptPropertyPanel.instructions");

    protected static Class codeClipsPanelClass;
    protected JEditorPane editorPane;

    // This is done to get around some module loading logic I dont
    // want to deal with at the moment.  How to get visibility to
    // com.sun.rave.toolbox.CodeClipsTab, from toolbox module
    public static void setCodeClipsPanelClass(Class clazz) {
        codeClipsPanelClass = clazz;
    }

    /**
     *
     */
    public JavaScriptPropertyPanel(JavaScriptPropertyEditor propertyEditor) {
        super(propertyEditor);
        initComponents((String) propertyEditor.getValue(), instructions);
    }

    protected JPanel getNewCodeClipsPanel() {
        // For now I do not want to have a hard reference and change the build order
//      return new CodeClipsTab();
        if (codeClipsPanelClass == null) {
            return null;
        }
        try {
            JPanel panel = (JPanel) codeClipsPanelClass.newInstance();
            return panel;
        } catch (Exception e) {
//            e.printStackTrace();
            // shouldn't totally hide this, but quicky jucky code anyway
            return null;
        }
    }

    public Object getPropertyValue() throws IllegalStateException {
        return editorPane.getText();
    }

//    public void addNotify() {
//        super.addNotify();
//        if (isEnabled() && isFocusable()) {
//            editorPane.requestFocus();
//        }
//    }

    protected void initComponents(String string, String instructions) {
        GridBagConstraints gridBagConstraints;
        setLayout(new java.awt.GridBagLayout());
        // Label with instructions
        final JLabel label = new JLabel(instructions);
        label.setFont(getFont());
        label.setDisplayedMnemonic(ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("JavaScriptPropertyPanel.label.mnemonic").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(label, gridBagConstraints);
        // Code clips panel
//        JPanel codeClips = getNewCodeClipsPanel();
//        if (codeClips != null) {
//            gridBagConstraints = new java.awt.GridBagConstraints();
//            gridBagConstraints.gridx = 0;
//            gridBagConstraints.gridy = 1;
//            gridBagConstraints.gridwidth = 1;
//            gridBagConstraints.fill = GridBagConstraints.BOTH;
//            gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
//            gridBagConstraints.weightx = 1.0;
//            gridBagConstraints.weighty = 1.0;
//            gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
//            add(codeClips, gridBagConstraints);
//        }
        // Editor pane
        JScrollPane scrollPane = new javax.swing.JScrollPane();
        editorPane = new JEditorPane();
        editorPane.setContentType("text/javascript"); // NOI18N
        editorPane.setText(string);
        editorPane.getAccessibleContext().setAccessibleName(ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString(
                "JavaScriptPropertyPanel.editor.accessibleName"));
        label.setLabelFor(editorPane);        
        
        //"Javascript editor"
        scrollPane.setViewportView(editorPane);        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        FontMetrics metrics = editorPane.getFontMetrics(editorPane.getFont());
        int columnWidth = metrics.charWidth('m'); // cloned code from JTextArea
        int rowHeight = metrics.getHeight();
        editorPane.setPreferredSize(new Dimension(columnWidth * 80, rowHeight * 15));
        editorPane.addFocusListener(new java.awt.event.FocusListener() {
            public void focusGained(java.awt.event.FocusEvent e) {
                editorPane.setSelectionStart(0);
                editorPane.setSelectionEnd(editorPane.getText().length());
            }
            
            public void focusLost(java.awt.event.FocusEvent e) {
                editorPane.setSelectionStart(0);
                editorPane.setSelectionEnd(0);
            }
        });
        add(scrollPane, gridBagConstraints);
    }
}
