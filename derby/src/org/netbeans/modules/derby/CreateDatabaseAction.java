/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.derby;

import java.awt.Dialog;
import java.io.File;
import org.netbeans.modules.derby.ui.CreateDatabasePanel;
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
        if (!RegisterDerby.getDefault().hasInstallLocation()) {
            RegisterDerby.showInformation(NbBundle.getMessage(RegisterDerby.class, "MSG_DerbyLocationIncorrect"));
            return;
        }
        
        CreateDatabasePanel panel = new CreateDatabasePanel();
        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(CreateDatabaseAction.class, "LBL_CreateDatabaseTitle"), true, null);
        panel.setDialogDescriptor(desc);
        panel.setDatabaseLocation(DerbyOptions.getDefault().getLastDatabaseLocation());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.setVisible(true);
        dialog.dispose();
        
        if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
            return;
        }
        
        String databaseLocation = panel.getDatabaseLocation();
        File databaseLocationFile = new File(databaseLocation);
        databaseLocationFile.mkdirs();
        String databaseName = new File(databaseLocationFile, panel.getDatabaseName()).getAbsolutePath();
        
        DerbyOptions.getDefault().setLastDatabaseLocation(databaseLocation);
        
        try {
            if (!RegisterDerby.getDefault().isRunning()) {
                RegisterDerby.getDefault().start(5000);
            }
            makeDatabase(databaseName);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }
    
    void makeDatabase(String dbname) throws Exception {
        RegisterDerby.getDefault().postCreateNewDatabase(new File(dbname));
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
