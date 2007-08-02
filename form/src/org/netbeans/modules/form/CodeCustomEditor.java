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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.editor.guards.SimpleSection;
import org.openide.util.Utilities;

/**
 * Custom editor for RADConnectionPropertyEditor. Allows editing the custom code
 * of a property.
 *
 * @author Tomas Pavek
 */
class CodeCustomEditor extends javax.swing.JPanel implements DocumentListener, Runnable {

    private RADConnectionPropertyEditor propertyEditor;

    private JEditorPane codePane;
    private boolean ignoreUpdate;

    public CodeCustomEditor(RADConnectionPropertyEditor propertyEditor,
                            FormModel formModel, FormProperty property)
    {
        this.propertyEditor = propertyEditor;
        JScrollPane jScrollPane = new JScrollPane() {
            // We want the editor pane's height to accommodate to the actual number
            // of lines. For that we also need to include the horizontal scrollbar
            // height into the preferred height. See also invokeUpdate method.
            public Dimension getPreferredSize() {
                Dimension prefSize = super.getPreferredSize();
                Component hBar = getHorizontalScrollBar();
                if (hBar != null && hBar.isVisible()) {
                    prefSize = new Dimension(prefSize.width, prefSize.height + hBar.getPreferredSize().height);
                }
                return prefSize;
            }
        };
        codePane = new JEditorPane();
        jScrollPane.setViewportView(codePane);
        JLabel headerLabel = new JLabel();
        JLabel footerLabel = new JLabel();
        JTextField typeField = new JTextField();

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutocreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .add(headerLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .add(3)
            .add(layout.createParallelGroup(GroupLayout.LEADING)
                .add(jScrollPane, GroupLayout.PREFERRED_SIZE, 320, Short.MAX_VALUE)
                .add(typeField))
            .add(3)
            .add(footerLabel));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .add(layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(jScrollPane.getInsets().top)
                    .add(layout.createParallelGroup().add(headerLabel).add(footerLabel)))
                .add(jScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .add(typeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

        int codePos = -1;
        FormDataObject dobj = FormEditor.getFormDataObject(formModel);
        SimpleSection sec = dobj.getFormEditorSupport().getInitComponentSection();
        if ((property instanceof RADProperty) && (property.getWriteMethod() != null)) {
            RADComponent metacomp = ((RADProperty)property).getRADComponent();
            headerLabel.setFont(codePane.getFont());
            headerLabel.setText("<html>" + metacomp.getName() + ".<b>" // NOI18N
                    + property.getWriteMethod().getName() + "</b>("); // NOI18N
            footerLabel.setFont(codePane.getFont());
            footerLabel.setText(");"); // NOI18N

            String codeSnippet;
            if (metacomp != formModel.getTopRADComponent()) {
                codeSnippet = " " + metacomp.getName() + "." + property.getWriteMethod().getName() + "("; // NOI18N
            } else {
                codeSnippet = " " + property.getWriteMethod().getName() + "("; // NOI18N
            }
            codePos = sec.getText().indexOf(codeSnippet);
            if (codePos >= 0) {
                codePos += codeSnippet.length();
            }
        }
        else {
            headerLabel.setText(FormUtils.getBundleString("CodeCustomEditor.codeLabel")); // NOI18N
        }
        if (codePos < 0) {
            codePos = sec.getText().indexOf('{') + 2;
        }
        FormUtils.setupEditorPane(codePane, dobj.getPrimaryFile(), codePos + sec.getStartPosition().getOffset());

        typeField.setBorder(BorderFactory.createEmptyBorder());
        typeField.setEditable(false);
        typeField.setFont(codePane.getFont());
        typeField.setText(Utilities.getClassName(property.getValueType()));

        codePane.getDocument().addDocumentListener(this);

        // issue 103809
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ev) {
                revalidate();
                repaint();
            }
        });
    }

    void setValue(RADConnectionPropertyEditor.RADConnectionDesignValue value) {
        if (value != null && value.getType() == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE) {
            ignoreUpdate = true;
            codePane.setText(value.getCode());
            ignoreUpdate = false;
        }
    }

    // DocumentListener
    public void insertUpdate(DocumentEvent e) {
        invokeUpdate();
    }

    // DocumentListener
    public void removeUpdate(DocumentEvent e) {
        invokeUpdate();
    }

    // DocumentListener
    public void changedUpdate(DocumentEvent e) {
    }

    private void invokeUpdate() {
        if (!ignoreUpdate) {
            ignoreUpdate = true;
            EventQueue.invokeLater(this); // set the value
            
            // also update the editor pane size according to the number of lines
            // (can't just track line count changes because the preferred height
            // also changes when the horizontal scrollbar appears/hides)
            revalidate();
            repaint();
        }
    }

    // updates the value in the property editor
    public void run() {
        propertyEditor.setValue(new RADConnectionPropertyEditor.RADConnectionDesignValue(codePane.getText()));
        ignoreUpdate = false;
    }
}
