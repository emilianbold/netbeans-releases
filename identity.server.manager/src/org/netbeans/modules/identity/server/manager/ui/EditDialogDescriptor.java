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

package org.netbeans.modules.identity.server.manager.ui;

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * This class is an extension of the DialogDescriptor for creating Edit
 * dialogs.
 *
 * Created on July 26, 2006, 10:51 PM
 *
 * @author ptliu
 */
public abstract class EditDialogDescriptor extends DialogDescriptor
        implements ChangeListener {
    public static final String STATUS_PREFIX = "Status:";      //NOI18N
   
    /** Creates a new instance of EditDialog */
    public EditDialogDescriptor(javax.swing.JPanel panel, String title,
            boolean add, JComponent[] components, HelpCtx helpCtx) {
        this(new InnerPanel(panel), title, add, components, helpCtx);
    }
   
    private EditDialogDescriptor(InnerPanel innerPanel, String title,
            boolean add, JComponent[] components, HelpCtx helpCtx) {
        super(innerPanel, getTitle(add, title), true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.BOTTOM_ALIGN,
                helpCtx,
                null);
        
        if (components != null) {
            DocListener listener = new DocListener(this);
            for (JComponent component : components) {
                if (component instanceof JTextField) {
                    ((JTextField) component).getDocument().addDocumentListener(listener);
                }
            }
        }
        
        checkValues();
    }
    
    private static String getTitle(boolean add, String title) {
        return (add ? NbBundle.getMessage(EditDialogDescriptor.class, "TTL_Add", title) :
            NbBundle.getMessage(EditDialogDescriptor.class, "TTL_Edit", title));
    }
    
    /** Calls validation of panel components, displays or removes the error message
     * Should be called from listeners listening to component changes.
     */
    public final void checkValues() {
        String errorMessage = validate();
        if (errorMessage==null) {
            setValid(true);
        } else {
            setValid(false);
        }
        javax.swing.JLabel errorLabel = ((InnerPanel)getMessage()).getErrorLabel();
        
        if (errorMessage != null) {
            if (errorMessage.startsWith(STATUS_PREFIX)) {
                errorMessage = errorMessage.substring(STATUS_PREFIX.length());
                errorLabel.setForeground(Color.BLACK);
            } else {
                errorLabel.setForeground(Color.RED);
            }
        }
        
        errorLabel.setText(errorMessage==null ? " " : errorMessage);    //NOI18N     
    }
    
    /** Provides validation for panel components */
    protected abstract String validate();
    
    public void stateChanged(ChangeEvent e) {
        checkValues();
        
        //innerPanel.repaint();
    }
    
    public JLabel getErrorLabel() {
        return ((InnerPanel)getMessage()).getErrorLabel();
    }
    
    private static class InnerPanel extends javax.swing.JPanel {
        javax.swing.JLabel errorLabel;
        InnerPanel(javax.swing.JPanel panel) {
            super(new java.awt.BorderLayout());
            errorLabel = new javax.swing.JLabel(" ");        //NOI18N
            errorLabel.setBorder(new javax.swing.border.EmptyBorder(12,12,0,0));
            errorLabel.setForeground(Color.RED);
            add(panel, java.awt.BorderLayout.CENTER);
            add(errorLabel, java.awt.BorderLayout.SOUTH);
        }
        
        void setErrorMessage(String message) {
            errorLabel.setText(message);
        }
        
        javax.swing.JLabel getErrorLabel() {
            return errorLabel;
        }
    }
    
    /** Useful DocumentListener class that can be added to the panel's text compoents */
    public static class DocListener implements javax.swing.event.DocumentListener {
        EditDialogDescriptor dialog;
        
        public DocListener(EditDialogDescriptor dialog) {
            this.dialog=dialog;
        }
        /**
         * Method from DocumentListener
         */
        public void changedUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
        
        /**
         * Method from DocumentListener
         */
        public void insertUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
        
        /**
         * Method from DocumentListener
         */
        public void removeUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
    }
    
    public interface Panel {
        JComponent[] getEditableComponents();
    }
}

