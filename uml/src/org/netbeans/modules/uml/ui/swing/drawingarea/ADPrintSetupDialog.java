/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * ADPrintSetupDialog.java
 *
 * Created on December 22, 2005, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.ui.swing.drawingarea;

import com.tomsawyer.editor.export.TSEPrintSetup;
import com.tomsawyer.editor.export.TSEPrintSetupDialog;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author Jyothi
 */
public class ADPrintSetupDialog extends TSEPrintSetupDialog {
    
    /** Creates a new instance of ADPrintSetupDialog */
    public ADPrintSetupDialog(
            Dialog owner,
            String title,
            TSEPrintSetup printSetup) {
        super(owner, title, printSetup);
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, "ESCAPE");
        this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
        this.getAccessibleContext().setAccessibleDescription(title);  // A11y
    }
    
}
