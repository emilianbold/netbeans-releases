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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper.swing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.wizard.WizardFrame;

public class NbiTextDialog extends NbiDialog {
    private WizardFrame   wizardFrame;
    
    private NbiTextPane   textPane;
    private NbiPanel      textPanel;
    private NbiScrollPane textScrollPane;
    
    private String title;
    private Text   text;
    
    public NbiTextDialog(WizardFrame owner, String title, Text text) {
        super(owner);
        
        this.wizardFrame = owner;
        this.title = title;
        this.text = text;
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        setTitle(title);
        
        textPane.setText(text);
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        setSize(wizardFrame.getSize());
        
        textPane = new NbiTextPane();
        
        textPanel = new NbiPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(textPane, BorderLayout.CENTER);
        
        textScrollPane = new NbiScrollPane(textPanel);
        textScrollPane.setViewportBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        add(textScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 11, 11), 0, 0));
    }
}