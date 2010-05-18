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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * A custom property editor for JavaScript, that displays code using basic
 * syntax highlighting.
 *
 * @author eric
 * @author gjmurphy
 */
public class JavaScriptPropertyPanel extends PropertyPanelBase {

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
        initComponents((String) propertyEditor.getValue());
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

    protected void initComponents(String string) {
        GridBagConstraints gridBagConstraints;
        setLayout(new java.awt.GridBagLayout());
        // Label with instructions
        final JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(JavaScriptPropertyPanel.class, "JavaScriptPropertyPanel.instructions"));
        label.setFont(getFont());
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
