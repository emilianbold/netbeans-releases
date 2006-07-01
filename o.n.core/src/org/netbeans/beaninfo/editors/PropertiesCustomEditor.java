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

package org.netbeans.beaninfo.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A custom editor for Properties.
 */
public class PropertiesCustomEditor extends JPanel implements DocumentListener {

    private PropertiesEditor editor;
    private JEditorPane editorPane;
    private JTextField warnings;
    
    public PropertiesCustomEditor(PropertiesEditor ed) {
        editor = ed;
        initComponents ();
        Properties props = (Properties) editor.getValue ();
        if (props == null) props = new Properties ();
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        try {
            props.store (baos, ""); // NOI18N
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        try {
            // Remove all comments from text.
            editorPane.setText(baos.toString("ISO-8859-1").replaceAll("(?m)^#.*" + System.getProperty("line.separator"), "")); // NOI18N
        } catch (UnsupportedEncodingException x) {
            throw new AssertionError(x);
        }
        setBorder (new EmptyBorder (new Insets(12, 12, 0, 11)));
        HelpCtx.setHelpIDString (this, PropertiesCustomEditor.class.getName ());
        
        editorPane.getAccessibleContext().setAccessibleName(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACS_PropertiesEditorPane"));
        editorPane.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACSD_PropertiesEditorPane"));
        editorPane.getDocument().addDocumentListener(this);
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACSD_CustomPropertiesEditor"));
    }
    
    public void insertUpdate(DocumentEvent e) {
        change();
    }

    public void removeUpdate(DocumentEvent e) {
        change();
    }

    public void changedUpdate(DocumentEvent e) {}

    private void change() {
        Properties v = new Properties();
        boolean loaded = false;
        try {
            v.load(new ByteArrayInputStream(editorPane.getText().getBytes("ISO-8859-1")));
            loaded = true;
        } catch (Exception x) { // IOException, IllegalArgumentException, maybe others
            Color c = UIManager.getColor("nb.errorForeground"); // NOI18N
            if (c != null) {
                warnings.setForeground(c);
            }
            warnings.setText(x.toString());
        }
        if (loaded) {
            editor.setValue(v);
            if (Pattern.compile("^#", Pattern.MULTILINE).matcher(editorPane.getText()).find()) { // #20996
                Color c = UIManager.getColor("nb.warningForeground"); // NOI18N
                if (c != null) {
                    warnings.setForeground(c);
                }
                warnings.setText(NbBundle.getMessage(PropertiesCustomEditor.class, "WARN_PropertiesComments"));
            } else {
                warnings.setText(null);
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        editorPane = new JEditorPane();
        editorPane.setContentType("text/x-properties"); // NOI18N
        add(new JScrollPane(editorPane), BorderLayout.CENTER);

        warnings = new JTextField(30);
        warnings.setEditable(false);
        add(warnings, BorderLayout.SOUTH);
    }
}
