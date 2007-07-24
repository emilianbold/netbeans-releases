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
package org.netbeans.modules.debugger.jpda.ui;

import java.awt.AWTKeyStroke;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Keymap;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import org.netbeans.api.java.source.JavaSource;

import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.ui.DialogBinding;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;

/**
 * A GUI panel for customizing a Watch.

 * @author Maros Sandor
 */
public class WatchPanel {

    private JPanel panel;
    private JEditorPane editorPane;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }
    
    public static void setupContext(JEditorPane editorPane) {
        DebuggerEngine en = DebuggerManager.getDebuggerManager ().getCurrentEngine();
        JPDADebugger d = (JPDADebugger) en.lookupFirst(null, JPDADebugger.class);
        CallStackFrame csf = d.getCurrentCallStackFrame();
        if (csf != null) {
            String language = DebuggerManager.getDebuggerManager ().getCurrentSession().getCurrentLanguage();
            SourcePath sp = (SourcePath) en.lookupFirst(null, SourcePath.class);
            String url = sp.getURL(csf, language);
            int line = csf.getLineNumber(language);
            setupContext(editorPane, url, line);
        }
    }
    
    public static void setupContext(JEditorPane editorPane, String url, int line) {
        EditorKit kit = CloneableEditorSupport.getEditorKit("text/x-java");
        editorPane.setEditorKit(kit);
        FileObject file;
        StyledDocument doc;
        try {
            file = URLMapper.findFileObject (new URL (url));
            if (file == null) {
                return;
            }
            try {
                DataObject dobj = DataObject.find (file);
                EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                if (ec == null) {
                    return;
                }
                try {
                    doc = ec.openDocument();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                    return;
                }
            } catch (DataObjectNotFoundException ex) {
                // null dobj
                return;
            }
        } catch (MalformedURLException e) {
            // null dobj
            return;
        }
        try {
            int offset = NbDocument.findLineOffset(doc, line);
            //editorPane.getDocument().putProperty(javax.swing.text.Document.StreamDescriptionProperty, dobj);
            JavaSource js = DialogBinding.bindComponentToFile(file, offset, 0, editorPane);
        } catch (IndexOutOfBoundsException ioobex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioobex);
        }
    }

    public JComponent getPanel() {
        if (panel != null) return panel;

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString ("CTL_Watch_Name")); // NOI18N
        editorPane = new JEditorPane();//expression); // NOI18N
        editorPane.setText(expression);
        
        setupContext(editorPane);//, EditorContextBridge.getContext().getCurrentURL (),
                     //EditorContextBridge.getContext().getCurrentLineNumber ());
        
        JScrollPane sp = createScrollableLineEditor(editorPane);
        FontMetrics fm = editorPane.getFontMetrics(editorPane.getFont());
        int size = 2*fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent() + 2;
        editorPane.setPreferredSize(new Dimension(30*size, (int) (1*size)));
        sp.setPreferredSize(new Dimension(30*size, (int) (1*size) + 2));
        
        textLabel.setBorder (new EmptyBorder (0, 0, 5, 0));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add (BorderLayout.NORTH, textLabel);
        panel.add (BorderLayout.CENTER, sp);
        
        editorPane.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        editorPane.setText (expression);
        editorPane.selectAll ();

        textLabel.setLabelFor (editorPane);
        editorPane.requestFocus ();
        
        return panel;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }
    
    public static JScrollPane createScrollableLineEditor(JEditorPane editorPane) {
        editorPane.setKeymap(new FilteredKeymap(editorPane));
        JScrollPane sp = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                
        editorPane.setBorder (
            new CompoundBorder (editorPane.getBorder (),
            new EmptyBorder (0, 0, 0, 0))
        );
        
        JTextField referenceTextField = new JTextField();
        
        int preferredHeight = referenceTextField.getPreferredSize().height;
        if (sp.getPreferredSize().height < preferredHeight) {
            sp.setPreferredSize(referenceTextField.getPreferredSize());
        }
        sp.setMinimumSize(sp.getPreferredSize());
        
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
        return sp;
    }
}
