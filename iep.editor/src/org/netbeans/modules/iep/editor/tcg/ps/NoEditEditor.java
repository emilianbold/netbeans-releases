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

package org.netbeans.modules.iep.editor.tcg.ps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditorSupport;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.StringTokenizer;

import javax.swing.ListModel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;


/**
 * Customized Property Editor for showing yes/no options
 *
 */
public class NoEditEditor extends TcgComponentNodePropertyEditor {
    private static final Logger mLogger = Logger.getLogger(NoEditEditor.class.getName());

    public NoEditEditor() {
    }
    
}

