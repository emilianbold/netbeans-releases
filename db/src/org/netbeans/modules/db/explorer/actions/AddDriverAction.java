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

package org.netbeans.modules.db.explorer.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import org.netbeans.api.db.explorer.DatabaseException;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.modules.db.explorer.dlg.AddDriverDialog;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;

public class AddDriverAction extends DatabaseAction {
    static final long serialVersionUID =-109193000951395612L;
    
    public void performAction(Node[] activatedNodes) {
        new AddDriverDialogDisplayer().showDialog();
    }
    
    public static final class AddDriverDialogDisplayer {
        
        private Dialog dialog;
        private JDBCDriver driver;
        
        public void showDialog() {
            final AddDriverDialog dlgPanel = new AddDriverDialog();

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        String name = dlgPanel.getDisplayName();
                        List drvLoc = dlgPanel.getDriverLocation();
                        String drvClass = dlgPanel.getDriverClass();

                        StringBuffer err = new StringBuffer();
                        if (drvLoc.size() < 1)
                            err.append(bundle().getString("AddDriverDialog_MissingFile")); //NOI18N
                        if (drvClass == null || drvClass.equals("")) {
                            if (err.length() > 0)
                                err.append(", "); //NOI18N
                            
                            err.append(bundle().getString("AddDriverDialog_MissingClass")); //NOI18N
                        }
                        if (err.length() > 0) {
                            String message = MessageFormat.format(bundle().getString("AddDriverDialog_ErrorMessage"), new String[] {err.toString()}); //NOI18N
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));

                            return;
                        }

                        if (dialog != null)
                            dialog.dispose();

                        //create driver instance and save it in the XML format
                        if (name == null || name.equals("")) // NOI18N
                            name = drvClass;

                        try {
                            driver = JDBCDriver.create(name, name, drvClass, (URL[]) drvLoc.toArray(new URL[drvLoc.size()]));
                            JDBCDriverManager.getDefault().addDriver(driver);
                        } catch (DatabaseException exc) {
                            //PENDING
                        }                    
                    }
                }
            };

            DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, bundle().getString("AddDriverDialogTitle"), true, actionListener); //NOI18N
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
        }
    }
}
