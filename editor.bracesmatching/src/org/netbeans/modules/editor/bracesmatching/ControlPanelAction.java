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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bracesmatching;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author vita
 */
public class ControlPanelAction extends TextAction {

    public ControlPanelAction() {
        super("match-brace-control-properties"); //NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        JTextComponent component = getTextComponent(e);
        ControlPanel panel = new ControlPanel(component);
        
        DialogDescriptor dd = new DialogDescriptor(
            panel, 
            "Braces Matching Control Panel", //NOI18N
            true,
            null
        ); 
        
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            panel.applyChanges();
        }
    }
    
}
