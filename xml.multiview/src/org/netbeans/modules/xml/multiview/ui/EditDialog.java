/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;
import org.openide.DialogDescriptor;

import org.openide.util.NbBundle;

/** EditDialog.java
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 */
public abstract class EditDialog extends DialogDescriptor {
    private javax.swing.JPanel panel;
    
    /** Creates a new instance of EditDialog */
    public EditDialog(javax.swing.JPanel panel, String title, boolean adding) {
        super (new InnerPanel(panel),getTitle(title,adding),true,
              DialogDescriptor.OK_CANCEL_OPTION,
              DialogDescriptor.OK_OPTION,
              DialogDescriptor.BOTTOM_ALIGN,
              null,
              null);
        this.panel=panel;
    }
   
    /** Creates a new instance of EditDialog */
    public EditDialog(javax.swing.JPanel panel, String title) {
        this(panel, title,false);
    }
    
    private static String getTitle(String title, boolean adding) {
        return (adding?NbBundle.getMessage(EditDialog.class,"TTL_ADD",title):
                NbBundle.getMessage(EditDialog.class,"TTL_EDIT",title));
    }
    /** Returns the dialog panel 
    * @return dialog panel
    */
    public final javax.swing.JPanel getDialogPanel() {
        return panel;
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
        errorLabel.setText(errorMessage==null?" ":errorMessage);
    }
    
    /** Provides validation for panel components */
    protected abstract String validate();
    
    private static class InnerPanel extends javax.swing.JPanel {
        javax.swing.JLabel errorLabel;
        InnerPanel(javax.swing.JPanel panel) {
            super(new java.awt.BorderLayout());
            errorLabel = new javax.swing.JLabel(" ");
            errorLabel.setBorder(new javax.swing.border.EmptyBorder(12,12,0,0));
            errorLabel.setForeground(SectionVisualTheme.getErrorLabelColor());
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
        EditDialog dialog;
        
        public DocListener(EditDialog dialog) {
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
}
