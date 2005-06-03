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
package org.netbeans.modules.form.actions;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ResourceBundle;
import javax.swing.*;

import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import org.netbeans.modules.form.*;

/**
 * Action that invokes custom property editor for the given property.
 *
 * @author Jan Stola
 */
public class PropertyAction extends AbstractAction {
    private static final String OK_COMMAND = "OK"; // NOI18N
    private static final String CANCEL_COMMAND = "Cancel"; // NOI18N
    private static final String RESTORE_COMMAND = "Restore"; // NOI18N
    private RADProperty property;
    private Dialog dialog;
    
    public PropertyAction(RADProperty property) {
        this.property = property;
        String name = (String)property.getValue("actionName"); // NOI18N
        if (name == null) {
            StringBuffer sb = new StringBuffer(property.getName());
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            name = sb.toString();
        }
        putValue(Action.NAME, name);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            PropertyEditor propEd = property.getPropertyEditor();
            propEd.setValue(property.getValue());
            final Component custEditor = propEd.getCustomEditor();
            Object[] options = buttons();
            DialogDescriptor descriptor = new DialogDescriptor(
                custEditor,
                (String)getValue(Action.NAME),
                true,
                options,
                DialogDescriptor.CANCEL_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String action = e.getActionCommand();
                            if (OK_COMMAND.equals(action)) {
                                Object value = ((EnhancedCustomPropertyEditor)custEditor).getPropertyValue();
                                property.setValue(value);
                            } else if (RESTORE_COMMAND.equals(action)) {
                                property.restoreDefaultValue();
                            }
                            dialog.dispose();
                        } catch (Exception ex) {
                            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                                NbBundle.getBundle(PropertyAction.class).getString("MSG_InvalidValue")); // NOI18N
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                });
            descriptor.setClosingOptions(new Object[0]);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.show();
            dialog = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private Object[] buttons() {
        ResourceBundle bundle = NbBundle.getBundle(PropertyAction.class);
        JButton okButton = new JButton(); 
        Mnemonics.setLocalizedText(okButton, bundle.getString("CTL_OK")); // NOI18N
        okButton.setActionCommand(OK_COMMAND);
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, bundle.getString("CTL_Cancel")); // NOI18N
        cancelButton.setActionCommand(CANCEL_COMMAND);
        if (property.isDefaultValue()) {
            return new Object[] {okButton, cancelButton};
        } else {
            JButton restoreButton = new JButton();
            Mnemonics.setLocalizedText(restoreButton, bundle.getString("CTL_RestoreDefault")); // NOI18N
            restoreButton.setActionCommand(RESTORE_COMMAND);
            return new Object[] {okButton, restoreButton, cancelButton};
        }
    }

}
