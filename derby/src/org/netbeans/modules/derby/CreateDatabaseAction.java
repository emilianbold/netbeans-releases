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


package org.netbeans.modules.derby;

import java.awt.Dialog;
import java.io.File;
import org.netbeans.modules.derby.ui.CreateDatabasePanel;
import org.netbeans.modules.derby.ui.DerbySystemHomePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author   Petr Jiricka
 */
public class CreateDatabaseAction extends CallableSystemAction {

    /** Generated sreial version UID. */
    //static final long serialVersionUID =3322896507302889271L;

    public CreateDatabaseAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
    
    public void performAction() {
        if (!Util.hasInstallLocation()) {
            Util.showInformation(NbBundle.getMessage(RegisterDerby.class, "MSG_DerbyLocationIncorrect"));
            return;
        }
        
        String derbySystemHome = DerbyOptions.getDefault().getSystemHome();
        if (derbySystemHome.length() <= 0) {
            derbySystemHome = DerbySystemHomePanel.findDerbySystemHome();
            if (derbySystemHome.length() > 0) {
                DerbyOptions.getDefault().setSystemHome(derbySystemHome);
            }
        }
        if (derbySystemHome.length() <= 0) {
            return;
        }
        
        CreateDatabasePanel panel = new CreateDatabasePanel(derbySystemHome);
        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(CreateDatabaseAction.class, "LBL_CreateDatabaseTitle"), true, null);
        panel.setDialogDescriptor(desc);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        String acsd = NbBundle.getMessage(CreateDatabaseAction.class, "ACSD_CreateDatabaseAction");
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        dialog.setVisible(true);
        dialog.dispose();
        
        if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
            return;
        }
        
        String databaseName = panel.getDatabaseName();
        String user = panel.getUser();
        String password = panel.getPassword();
        
        // if only the username or password is null, ensure they are both null
        if (user == null || password == null) {
            user = null;
            password = null;
        }
        
        try {
            makeDatabase(databaseName, user, password);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }
    
    void makeDatabase(String dbname, String user, String password) throws Exception {
        RegisterDerby.getDefault().postCreateNewDatabase(dbname, user, password);
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return NbBundle.getBundle(CreateDatabaseAction.class).getString("LBL_CreateDBAction");
    }

    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateDatabaseAction.class);
    }

}
