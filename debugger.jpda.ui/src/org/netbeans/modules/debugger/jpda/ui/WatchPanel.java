/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.ui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;

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

    public JComponent getPanel() {
        if (panel != null) return panel;

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel (bundle.getString ("CTL_Watch_Name")); // NOI18N
        editorPane = new JEditorPane("text/x-java", expression); // NOI18N
        editorPane.setKeymap(new FilteredKeymap(editorPane.getKeymap()));
        
        DebuggerEngine en = DebuggerManager.getDebuggerManager ().getCurrentEngine();
        JPDADebugger d = (JPDADebugger) en.lookupFirst(null, JPDADebugger.class);
        CallStackFrame csf = d.getCurrentCallStackFrame();
        if (csf != null) {
            DataObject dobj = null;
            SourcePath sp = (SourcePath) en.lookupFirst(null, SourcePath.class);
            String url = sp.getURL(csf, "Java");
            FileObject file;
            try {
                file = URLMapper.findFileObject (new URL (url));
                if (file != null) {
                    try {
                        dobj = DataObject.find (file);
                    } catch (DataObjectNotFoundException ex) {
                        // null dobj
                    }
                }
            } catch (MalformedURLException e) {
                // null dobj
            }
            editorPane.getDocument().putProperty(javax.swing.text.Document.StreamDescriptionProperty, dobj);
        }
        
        JScrollPane sp = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        textLabel.setBorder (new EmptyBorder (0, 0, 5, 0));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add (BorderLayout.NORTH, textLabel);
        panel.add (BorderLayout.CENTER, sp);
        
        FontMetrics fm = editorPane.getFontMetrics(editorPane.getFont());
        int size = 2*fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent() + 4;
        
        editorPane.setPreferredSize(new Dimension(30*size, (int) (1*size)));
        
        editorPane.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        editorPane.setBorder (
            new CompoundBorder (editorPane.getBorder (),
            new EmptyBorder (2, 0, 2, 0))
        );
        textLabel.setDisplayedMnemonic (
            bundle.getString ("CTL_Watch_Name_Mnemonic").charAt (0) // NOI18N
        );
        editorPane.setText (expression);
        editorPane.selectAll ();

        textLabel.setLabelFor (editorPane);
        editorPane.requestFocus ();
        
        return panel;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }
}
