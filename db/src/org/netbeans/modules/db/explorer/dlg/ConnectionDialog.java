/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;

public class ConnectionDialog {

    private transient JTabbedPane tabs;
    private transient SchemaPanel schemaPanel;
    private transient JPanel basePane;
    private transient Exception storedExp;

    static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
        
    Dialog dialog = null;
    
    public ConnectionDialog(JPanel basePane, 
                            JPanel extendPane, 
                            String dlgTitle, 
                            ActionListener actionListener, 
                            ChangeListener tabListener) {
                                
        if(basePane.equals(extendPane))
            return;

        tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        basePane = basePane;
        schemaPanel = (SchemaPanel)extendPane;
        
        tabs.addChangeListener(tabListener);

        // base panel for set base info for connection
        tabs.addTab( bundle.getString("BasePanelTitle"), // NOI18N
                    /*icon*/ null, basePane, 
                    bundle.getString("BasePanelHint") ); // NOI18N

        // extend panel for select schema name
        tabs.addTab( bundle.getString("ExtendPanelTitle"), // NOI18N
                    /*icon*/ null, 
                    extendPane, 
                    bundle.getString("ExtendPanelHint") ); // NOI18N

        DialogDescriptor descriptor = new DialogDescriptor(tabs, dlgTitle, true, actionListener); //NOI18N
        // inbuilt close of the dialog is only after CANCEL button click
        // after OK button is dialog closed by hand
        Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
        descriptor.setClosingOptions(closingOptions);
        dialog = TopManager.getDefault().createDialog(descriptor);
        dialog.setResizable(false);
        dialog.setVisible(false);
    }
    
    public void close() {

        // dialog is closed after successfully create connection
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    public void setVisible(boolean mode) {
        dialog.setVisible(mode);
    }
    
    public void setSelectedComponent(JPanel panel) {
        tabs.setSelectedComponent(panel);
    }
    
    public void setException(Exception e) {
        storedExp = e;
    }
    
    public boolean isException() {
        return (storedExp != null);
    }
}
